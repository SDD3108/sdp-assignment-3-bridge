package kz.aitu.gateway.sender;

public interface MessageSender {
    boolean sendMessage(String recipient, String title, String body);
    boolean isAvailable();
    String getChannelName();
}
