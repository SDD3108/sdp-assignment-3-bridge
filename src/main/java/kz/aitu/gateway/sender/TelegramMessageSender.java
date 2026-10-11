package kz.aitu.gateway.sender;

public final class TelegramMessageSender extends SimulatedMessageSender {
    public TelegramMessageSender() { this(true); }
    public TelegramMessageSender(boolean available) { super(available); }

    @Override public String getChannelName() { return "Telegram"; }

    @Override
    protected String formatPayload(String recipient, String title, String body) {
        return "parse_mode=Markdown; **" + title + "**\n" + body;
    }
}
