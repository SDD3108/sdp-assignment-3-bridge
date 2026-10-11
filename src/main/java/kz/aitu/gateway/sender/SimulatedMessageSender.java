package kz.aitu.gateway.sender;

import java.util.ArrayList;
import java.util.List;

public abstract class SimulatedMessageSender implements MessageSender {
    private boolean available;
    private final List<DeliveredMessage> deliveries = new ArrayList<>();

    protected SimulatedMessageSender(boolean available) {
        this.available = available;
    }

    @Override
    public final boolean isAvailable() {
        return available;
    }

    public final void setAvailable(boolean available) {
        this.available = available;
    }

    public final List<DeliveredMessage> getDeliveries() {
        return List.copyOf(deliveries);
    }

    @Override
    public final boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || body.isBlank()) {
            System.out.println("[" + getChannelName() + "] FAILED: unavailable or invalid message");
            return false;
        }
        if (!accepts(body)) {
            System.out.println("[" + getChannelName() + "] FAILED: channel restriction");
            return false;
        }
        String payload = formatPayload(recipient, title, body);
        deliveries.add(new DeliveredMessage(recipient, title, body, payload));
        System.out.println("[" + getChannelName() + "] SENT to " + recipient + ": " + payload);
        return true;
    }

    protected boolean accepts(String body) {
        return true;
    }

    protected abstract String formatPayload(String recipient, String title, String body);
}
