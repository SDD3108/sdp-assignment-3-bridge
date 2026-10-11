package kz.aitu.gateway;

import java.util.List;
import kz.aitu.gateway.notification.*;
import kz.aitu.gateway.sender.*;

public final class Demo {
    private Demo() { }

    public static void main(String[] args) {
        TelegramMessageSender telegram = new TelegramMessageSender();
        EmailMessageSender email = new EmailMessageSender();
        SmsMessageSender sms = new SmsMessageSender();

        section("1. UrgentNotification + Telegram");
        Notification urgent = new UrgentNotification(telegram, sms);
        urgent.send("student-01", "Submit the assignment today.");

        section("2. EncryptedNotification + Email");
        new EncryptedNotification(email).send("student-01", "Your verification code is 1234.");

        section("3. ScheduledBatchNotification + SMS");
        new ScheduledBatchNotification(sms, List.of("student-01", "student-02", ""))
                .sendBatch("Design Patterns defense starts at 10:00.");

        section("4. Runtime switching: Telegram -> Email (same object)");
        urgent.send("student-01", "Before switching.");
        urgent.setSender(email);
        urgent.send("student-01", "After switching.");

        section("5. Telegram unavailable -> fallback to SMS");
        telegram.setAvailable(false);
        urgent.setSender(telegram);
        urgent.send("student-01", "Fallback keeps the notification deliverable.");

        section("6. MarketingNotification + PushNotificationSender");
        new MarketingNotification(new PushNotificationSender())
                .send("device-01", "Get 20% off the Java workshop.");

        section("Extra: Kafka event publishing");
        new EncryptedNotification(new KafkaEventSender()).send("student-01", "Event delivered.");
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
