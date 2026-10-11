package kz.aitu.gateway.notification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import kz.aitu.gateway.sender.MessageSender;

public final class EncryptedNotification extends Notification {
    public EncryptedNotification(MessageSender sender){
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        if (message == null || message.isBlank()) {
            report(false);
            return;
        }
        String encoded = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        report(sender.sendMessage(recipient, "Encoded notification (Base64)", encoded));
    }
}
