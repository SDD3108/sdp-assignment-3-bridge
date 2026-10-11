# Подготовка к защите Assignment 3

Проект моделирует отправку уведомлений по разным каналам. Главное на защите — показать, что тип уведомления и канал доставки изменяются независимо. Отправки симулируются, поэтому интернет и реальные аккаунты Telegram или Kafka не нужны.

## Краткое объяснение архитектуры

«В проекте есть две иерархии. `Notification` отвечает за поведение уведомления: срочность, кодирование, пакетную отправку или рекламу. `MessageSender` отвечает за способ доставки: Telegram, Email, SMS, Kafka или Push. Поле `sender` внутри `Notification` связывает их через композицию. Это Bridge: я могу добавлять новый тип уведомления, не меняя каналы, и новый канал, не меняя уведомления».

Открой сначала `Notification.java`, затем `MessageSender.java`. Покажи поле `protected MessageSender sender`, конструктор и `setSender`. Потом открой `UrgentNotification.java`: он использует только интерфейс и нигде не проверяет конкретный класс канала.

`SimulatedMessageSender` — вспомогательный абстрактный класс. Он хранит доступность и успешные отправки, проверяет входные данные и выводит результат. Конкретные каналы переопределяют форматирование; SMS дополнительно проверяет длину. Сам Bridge находится между `Notification` и `MessageSender`, а не между двумя классами симулятора.

## Демонстрация

Из корня проекта выполни:

```sh
mvn -B -ntp verify
java -jar target/notification-gateway-1.0.0.jar
```

1. Покажи `[URGENT]` и форматирование Telegram.
2. Покажи Base64 в Email. Объясни: это допустимое учебное преобразование, но не настоящая криптография.
3. Покажи итог пакета `Total: 3`, `Successful: 2`, `Failed: 1`. Третий получатель пустой специально.
4. Покажи переменную `urgent` до и после `setSender(email)`. Объект уведомления тот же, заменился только объект канала.
5. Покажи `telegram.setAvailable(false)`: срочное уведомление проверяет доступность и переходит на SMS.
6. Покажи `MarketingNotification` вместе с `PushNotificationSender`.
7. Дополнительно покажи Kafka: это вывод события с topic и key, без настоящего брокера.

## Типичные вопросы

**Почему не наследовать UrgentNotification от TelegramMessageSender?** Потому что срочность и доставка — разные обязанности. Такое наследование связывает их и приводит к классам для каждой комбинации. Композиция позволяет менять канал во время работы программы.

**Почему Bridge, а не Adapter?** Здесь заранее спроектированы две независимые стороны. Adapter нужен, когда уже существует несовместимый интерфейс и его надо привести к ожидаемому. Мы ничего такого не преобразуем.

**Почему `send` возвращает void?** Так задан контракт в условии. Канал возвращает boolean, уведомления обрабатывают и выводят результат. Для пакетной отправки есть дополнительный метод `sendBatch`, который возвращает `BatchSummary`.

**Где таймер в ScheduledBatchNotification?** В обязательных требованиях указаны коллекция получателей, отправка всем и статистика. Поэтому пакет выполняется явно методом `sendBatch` в выбранный вызывающим кодом момент. Фоновый планировщик не реализован.

**Что произойдёт при ошибке основного канала?** Если он недоступен, метод отправки не вызывается. Если доступен, но возвращает false, тоже пробуем fallback. Запасные каналы перебираются по порядку; после первого успеха останавливаемся. Если все неудачны — выводится failed.

**Какие ограничения у SMS?** Максимум 160 Unicode code points в итоговом body. Заголовок SMS не использует, а префиксы `[URGENT]` и `[PROMO]` входят в лимит. Сообщение не обрезается: превышение приводит к false. Реальные правила сегментации SMS сложнее и здесь не моделируются.

**Как доказать независимое расширение?** Выполни `git log --oneline`. Второй коммит добавляет Push и Marketing. Выполни `git show --stat ff082af`: среди существующих production-файлов меняется только `Demo.java`, где собирается новая комбинация. Старые уведомления и старые каналы не меняются. Тесты проверяют Push со старыми уведомлениями и Marketing со всеми каналами.

## Изменения, которые могут попросить

### Добавить WhatsAppMessageSender

Создай файл `src/main/java/kz/aitu/gateway/sender/WhatsAppMessageSender.java`:

```java
package kz.aitu.gateway.sender;

public final class WhatsAppMessageSender extends SimulatedMessageSender {
    public WhatsAppMessageSender() { super(true); }

    @Override
    public String getChannelName() { return "WhatsApp"; }

    @Override
    protected String formatPayload(String recipient, String title, String body) {
        return "WhatsApp: *" + title + "* " + body;
    }
}
```

В `Demo` добавь `new UrgentNotification(new WhatsAppMessageSender()).send("student", "Hello");`. Существующие уведомления менять не нужно. Добавь тест успешной отправки нового канала, затем выполни сборку и демонстрацию.

### Уменьшить лимит SMS до 120

В `SmsMessageSender` измени `MAX_CHARACTERS` на `120`. Обнови граничные тесты: 120 символов успешно, 121 — ошибка. Для теста префикса urgent используй 112 символов исходного текста: 9 + 112 = 121. Аналогично обнови Unicode-тест и описание лимита в README. Тесты должны проверять новое требование, а не старую константу.

### Добавить SilentNotification

Уточни значение «silent»: например, уведомление без префикса срочности с заголовком `Silent notification`. Создай наследника `Notification`, реализуй `send` через `sender.sendMessage` и `report`. Если требуется именно отключение звука устройства, текущий интерфейс не содержит такого параметра; это уже отдельное изменение контракта, а не просто новый текст уведомления.

### Сделать Email недоступным и использовать другой канал

```java
var email = new EmailMessageSender(false);
var notification = new UrgentNotification(email, new SmsMessageSender());
notification.send("student", "Please respond.");
```

### Добавить второй fallback

Конструктор уже принимает несколько реализаций интерфейса:

```java
var notification = new UrgentNotification(
    new TelegramMessageSender(false),
    new SmsMessageSender(false),
    new EmailMessageSender()
);
notification.send("student", "Please respond.");
```

Ожидается пропуск Telegram и SMS, затем успешная отправка Email. `UrgentNotification` изменять не нужно.

### Сменить канал во время выполнения

```java
Notification notification = new EncryptedNotification(new EmailMessageSender());
notification.send("student", "First");
notification.setSender(new PushNotificationSender());
notification.send("student", "Second");
```

Оба сообщения преобразуются в Base64, потому что тип уведомления не изменился. Меняется только доставка.

## Перед сдачей

Открой репозиторий на GitHub, проверь README и UML, запусти тесты и демо. Отправь ссылку на репозиторий в Moodle. Потренируйся объяснять код и выполнять хотя бы одно изменение из списка: работающее приложение само по себе не заменяет индивидуальную защиту.
