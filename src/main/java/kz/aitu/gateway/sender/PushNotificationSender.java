package kz.aitu.gateway.sender;

/** New concrete implementor; no notification class needs to know about this type. */
public final class PushNotificationSender extends SimulatedMessageSender {
    public PushNotificationSender() { this(true); }
    public PushNotificationSender(boolean available) { super(available); }

    @Override public String getChannelName() { return "Push"; }

    @Override
    protected String formatPayload(String recipient, String title, String body) {
        return "device=" + recipient + "; push-alert=[" + title + "] " + body;
    }
}
