package kz.aitu.gateway;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import kz.aitu.gateway.notification.*;
import kz.aitu.gateway.sender.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GatewayTest {
    @Test void telegramDeliversFormattedMessage() {
        var sender = new TelegramMessageSender();
        assertTrue(sender.sendMessage("student", "Title", "Hello"));
        assertEquals("parse_mode=Markdown; **Title**\nHello", sender.getDeliveries().get(0).payload());
    }

    @Test void emailIncludesSubjectAndSignature() {
        var sender = new EmailMessageSender();
        assertTrue(sender.sendMessage("student", "Title", "Hello"));
        assertEquals("Subject: Title\nHello\n--\nNotification Gateway", sender.getDeliveries().get(0).payload());
    }

    @Test void unavailableProviderRejectsDelivery() {
        var sender = new TelegramMessageSender(false);
        assertFalse(sender.isAvailable());
        assertFalse(sender.sendMessage("student", "Title", "Hello"));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test void providerCanRecover() {
        var sender = new EmailMessageSender(false);
        sender.setAvailable(true);
        assertTrue(sender.sendMessage("student", "Title", "Hello"));
    }

    @Test void urgentAddsPrefixAndDoesNotUseFallbackAfterSuccess() {
        var primary = new TelegramMessageSender();
        var fallback = new SmsMessageSender();
        new UrgentNotification(primary, fallback).send("student", "Act now");
        assertEquals("[URGENT] Act now", primary.getDeliveries().get(0).body());
        assertTrue(fallback.getDeliveries().isEmpty());
    }

    @Test void urgentUsesFallbackWhenPrimaryUnavailable() {
        var primary = new TelegramMessageSender(false);
        var fallback = new SmsMessageSender();
        new UrgentNotification(primary, fallback).send("student", "Act now");
        assertTrue(primary.getDeliveries().isEmpty());
        assertEquals("[URGENT] Act now", fallback.getDeliveries().get(0).body());
    }

    @Test void fallbackUsesOnlyInterfaceAndSkipsUnavailableProviders() {
        var primary = new RecordingSender(false, true);
        var firstFallback = new RecordingSender(false, true);
        var lastFallback = new RecordingSender(true, true);
        new UrgentNotification(primary, firstFallback, lastFallback).send("student", "Hello");
        assertEquals(0, primary.calls);
        assertEquals(0, firstFallback.calls);
        assertEquals(1, lastFallback.calls);
        assertEquals("[URGENT] Hello", lastFallback.body);
    }

    @Test void fallbackAlsoHandlesAvailableProviderReturningFalse() {
        var primary = new RecordingSender(true, false);
        var fallback = new RecordingSender(true, true);
        new UrgentNotification(primary, fallback).send("student", "Hello");
        assertEquals(1, primary.calls);
        assertEquals(1, fallback.calls);
    }

    @Test void allUnavailableProvidersProduceNoDelivery() {
        var primary = new TelegramMessageSender(false);
        var fallback = new SmsMessageSender(false);
        assertDoesNotThrow(() -> new UrgentNotification(primary, fallback).send("student", "Hello"));
        assertTrue(primary.getDeliveries().isEmpty());
        assertTrue(fallback.getDeliveries().isEmpty());
    }

    @Test void smsAcceptsExactly160Characters() {
        var sender = new SmsMessageSender();
        assertTrue(sender.sendMessage("student", "Ignored title", "a".repeat(160)));
        assertEquals("a".repeat(160), sender.getDeliveries().get(0).payload());
    }

    @Test void smsRejects161CharactersWithoutTruncation() {
        var sender = new SmsMessageSender();
        assertFalse(sender.sendMessage("student", "Title", "a".repeat(161)));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test void smsCountsUnicodeCodePoints() {
        var sender = new SmsMessageSender();
        assertTrue(sender.sendMessage("student", "Title", "\uD83D\uDE00".repeat(160)));
        assertFalse(sender.sendMessage("student", "Title", "\uD83D\uDE00".repeat(161)));
    }

    @Test void urgentPrefixCountsTowardsSmsLimit() {
        var sender = new SmsMessageSender();
        new UrgentNotification(sender).send("student", "a".repeat(152));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test void encryptedNotificationEncodesUtf8BeforeDelivery() {
        var sender = new EmailMessageSender();
        String original = "Secret: \u0421\u04d9\u043b\u0435\u043c!";
        new EncryptedNotification(sender).send("student", original);
        String body = sender.getDeliveries().get(0).body();
        assertNotEquals(original, body);
        assertEquals(original, new String(Base64.getDecoder().decode(body), StandardCharsets.UTF_8));
    }

    @Test void batchCountsSuccessesAndInvalidRecipients() {
        var sender = new SmsMessageSender();
        var batch = new ScheduledBatchNotification(sender, Arrays.asList("one", "two", "", null));
        assertEquals(new BatchSummary(4, 2, 2), batch.sendBatch("Meeting"));
        assertEquals(List.of("one", "two"), sender.getDeliveries().stream().map(DeliveredMessage::recipient).toList());
    }

    @Test void batchFailsForEveryRecipientWhenUnavailable() {
        var batch = new ScheduledBatchNotification(new SmsMessageSender(false), List.of("one", "two"));
        assertEquals(new BatchSummary(2, 0, 2), batch.sendBatch("Meeting"));
    }

    @Test void emptyBatchProducesZeroSummary() {
        var batch = new ScheduledBatchNotification(new SmsMessageSender(), List.of());
        assertEquals(new BatchSummary(0, 0, 0), batch.sendBatch("Meeting"));
    }

    @Test void repeatedBatchDoesNotAccumulateSummaryAndCopiesRecipients() {
        var recipients = new ArrayList<>(List.of("one"));
        var batch = new ScheduledBatchNotification(new SmsMessageSender(), recipients);
        recipients.add("two");
        assertEquals(new BatchSummary(1, 1, 0), batch.sendBatch("First"));
        assertEquals(new BatchSummary(1, 1, 0), batch.sendBatch("Second"));
    }

    @Test void runtimeSwitchRedirectsSameNotificationObject() {
        var telegram = new TelegramMessageSender();
        var email = new EmailMessageSender();
        Notification notification = new UrgentNotification(telegram);
        notification.send("student", "First");
        notification.setSender(email);
        notification.send("student", "Second");
        assertEquals(1, telegram.getDeliveries().size());
        assertEquals("[URGENT] First", telegram.getDeliveries().get(0).body());
        assertEquals("[URGENT] Second", email.getDeliveries().get(0).body());
    }

    @Test void kafkaSimulatesEventWithTopicAndKey() {
        var sender = new KafkaEventSender();
        assertTrue(sender.sendMessage("student", "Title", "Hello"));
        assertEquals("topic=notifications; key=student; event={title='Title', body='Hello'}",
                sender.getDeliveries().get(0).payload());
    }

    @Test void invalidInputsDoNotBecomeSuccessfulMessages() {
        var sender = new EmailMessageSender();
        assertFalse(sender.sendMessage(" ", "Title", "Hello"));
        assertFalse(sender.sendMessage(null, "Title", "Hello"));
        assertFalse(sender.sendMessage("student", null, "Hello"));
        assertFalse(sender.sendMessage("student", "Title", null));
        new UrgentNotification(sender).send("student", " ");
        new EncryptedNotification(sender).send("student", null);
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test void nullSenderIsRejectedBeforeAndAfterConstruction() {
        assertThrows(NullPointerException.class, () -> new UrgentNotification(null));
        var sender = new EmailMessageSender();
        var notification = new UrgentNotification(sender);
        assertThrows(NullPointerException.class, () -> notification.setSender(null));
        notification.send("student", "Still works");
        assertEquals(1, sender.getDeliveries().size());
    }

    private static final class RecordingSender implements MessageSender {
        private final boolean available;
        private final boolean result;
        private int calls;
        private String body;

        private RecordingSender(boolean available, boolean result) {
            this.available = available;
            this.result = result;
        }
        public boolean isAvailable() { return available; }
        public String getChannelName() { return "Test channel"; }
        public boolean sendMessage(String recipient, String title, String body) {
            calls++;
            this.body = body;
            return result;
        }
    }
}
