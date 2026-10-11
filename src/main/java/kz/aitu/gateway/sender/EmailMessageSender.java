package kz.aitu.gateway.sender;

public final class EmailMessageSender extends SimulatedMessageSender {
    public EmailMessageSender() { this(true); }
    public EmailMessageSender(boolean available) { super(available); }

    @Override public String getChannelName() { return "Email"; }

    @Override
    protected String formatPayload(String recipient, String title, String body) {
        return "Subject: " + title + "\n" + body + "\n--\nNotification Gateway";
    }
}
