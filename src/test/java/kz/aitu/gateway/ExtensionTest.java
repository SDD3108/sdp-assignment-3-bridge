package kz.aitu.gateway;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import kz.aitu.gateway.notification.*;
import kz.aitu.gateway.sender.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionTest {
    @Test void pushDeliversDeviceAlert() {
        var sender = new PushNotificationSender();
        assertTrue(sender.sendMessage("device-01", "Offer", "Hello"));
        assertEquals("device=device-01; push-alert=[Offer] Hello", sender.getDeliveries().get(0).payload());
    }

    @Test void unavailablePushRejectsMessage() {
        var sender = new PushNotificationSender(false);
        assertFalse(sender.sendMessage("device-01", "Offer", "Hello"));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test void existingUrgentNotificationWorksWithPush() {
        var sender = new PushNotificationSender();
        new UrgentNotification(sender).send("device-01", "Hello");
        assertEquals("[URGENT] Hello", sender.getDeliveries().get(0).body());
    }

    @Test void existingEncryptedNotificationWorksWithPush() {
        var sender = new PushNotificationSender();
        new EncryptedNotification(sender).send("device-01", "Secret");
        assertEquals("Secret", new String(Base64.getDecoder().decode(sender.getDeliveries().get(0).body()),
                StandardCharsets.UTF_8));
    }

    @Test void existingBatchNotificationWorksWithPush() {
        var sender = new PushNotificationSender();
        assertEquals(new BatchSummary(2, 2, 0),
                new ScheduledBatchNotification(sender, List.of("one", "two")).sendBatch("Hello"));
        assertEquals(2, sender.getDeliveries().size());
    }

    @ParameterizedTest
    @MethodSource("providers")
    void marketingWorksWithEveryProvider(SimulatedMessageSender sender) {
        new MarketingNotification(sender).send("student", "Save 20% today.");
        assertEquals(1, sender.getDeliveries().size());
        assertEquals("Special offer", sender.getDeliveries().get(0).title());
        assertEquals("[PROMO] Save 20% today.\nReply STOP to unsubscribe.", sender.getDeliveries().get(0).body());
    }

    @Test void marketingRespectsSmsLengthFailure() {
        var sender = new SmsMessageSender();
        new MarketingNotification(sender).send("student", "a".repeat(150));
        assertTrue(sender.getDeliveries().isEmpty());
    }

    @Test void marketingRejectsBlankMessageBeforeAddingFooter() {
        var sender = new PushNotificationSender();
        new MarketingNotification(sender).send("student", " ");
        assertTrue(sender.getDeliveries().isEmpty());
    }

    private static Stream<SimulatedMessageSender> providers() {
        return Stream.of(new TelegramMessageSender(), new EmailMessageSender(), new SmsMessageSender(),
                new KafkaEventSender(), new PushNotificationSender());
    }
}
