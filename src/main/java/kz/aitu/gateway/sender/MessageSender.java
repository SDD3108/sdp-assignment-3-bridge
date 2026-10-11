package kz.aitu.gateway.sender;

/** Implementor: every notification talks to this contract, not a concrete channel. */
public interface MessageSender {
    boolean sendMessage(String recipient, String title, String body);
    boolean isAvailable();
    String getChannelName();
}
