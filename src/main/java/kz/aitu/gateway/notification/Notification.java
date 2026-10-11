package kz.aitu.gateway.notification;

import java.util.Objects;
import kz.aitu.gateway.sender.MessageSender;

/** Abstraction. The sender reference is the Bridge to the implementation hierarchy. */
public abstract class Notification {
    protected MessageSender sender;

    protected Notification(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    public void setSender(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    public abstract void send(String recipient, String message);

    protected final void report(boolean delivered) {
        System.out.println("Result: " + (delivered ? "successful" : "failed"));
    }
}
