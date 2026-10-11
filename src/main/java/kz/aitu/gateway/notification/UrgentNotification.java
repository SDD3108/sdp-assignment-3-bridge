package kz.aitu.gateway.notification;

import java.util.List;
import kz.aitu.gateway.sender.MessageSender;

public final class UrgentNotification extends Notification {
    private final List<MessageSender> fallbackSenders;

    public UrgentNotification(MessageSender sender, MessageSender... fallbackSenders) {
        super(sender);
        this.fallbackSenders = List.of(fallbackSenders);
    }

    @Override
    public void send(String recipient, String message) {
        if (message == null || message.isBlank()) {
            report(false);
            return;
        }
        String body = "[URGENT] " + message;
        if (trySend(sender, recipient, body)) {
            report(true);
            return;
        }
        for (MessageSender fallback : fallbackSenders) {
            System.out.println("Trying fallback: " + fallback.getChannelName());
            if (trySend(fallback, recipient, body)) {
                report(true);
                return;
            }
        }
        report(false);
    }

    private boolean trySend(MessageSender candidate, String recipient, String body) {
        if(!candidate.isAvailable()){
            System.out.println(candidate.getChannelName() + " unavailable");
            return false;
        }
        return candidate.sendMessage(recipient, "Urgent notification", body);
    }
}
