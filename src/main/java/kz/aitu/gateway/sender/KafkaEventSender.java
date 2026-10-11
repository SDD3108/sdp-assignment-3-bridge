package kz.aitu.gateway.sender;

public final class KafkaEventSender extends SimulatedMessageSender {
    public KafkaEventSender() {
        this(true);
    }
    public KafkaEventSender(boolean available) {
        super(available);
    }

    @Override public String getChannelName() {
        return "Kafka";
    }

    @Override
    protected String formatPayload(String recipient, String title, String body) {
        return "topic=notifications; key=" + recipient + "; event={title='" + title + "', body='" + body + "'}";
    }
}
