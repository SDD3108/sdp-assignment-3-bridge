package kz.aitu.gateway.notification;

import kz.aitu.gateway.sender.MessageSender;

public final class MarketingNotification extends Notification {
    public MarketingNotification(MessageSender sender){
        super(sender);
    }

    @Override
    public void send(String recipient, String message) {
        if (message == null || message.isBlank()) {
            report(false);
            return;
        }
        String body = "[PROMO] " + message + "\nReply STOP to unsubscribe.";
        report(sender.sendMessage(recipient, "Special offer", body));
    }
}
