package kz.aitu.gateway.sender;

/** A successful simulated delivery, retained so its content can be inspected. */
public record DeliveredMessage(String recipient, String title, String body, String payload) {
}
