package kz.aitu.gateway.sender;

public record DeliveredMessage(String recipient, String title, String body, String payload) {
}
