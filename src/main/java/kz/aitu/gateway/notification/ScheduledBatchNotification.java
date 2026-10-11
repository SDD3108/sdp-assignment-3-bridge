package kz.aitu.gateway.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import kz.aitu.gateway.sender.MessageSender;

/** A prepared batch executed by an explicit sendBatch call (no timer/thread required). */
public final class ScheduledBatchNotification extends Notification {
    private final List<String> recipients;

    public ScheduledBatchNotification(MessageSender sender, Collection<String> recipients) {
        super(sender);
        // Copy the collection; invalid entries count as failures when the batch is sent.
        this.recipients = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(recipients)));
    }

    @Override
    public void send(String recipient, String message) {
        report(sender.sendMessage(recipient, "Batch notification", message));
    }

    public BatchSummary sendBatch(String message) {
        int successful = 0;
        for (String recipient : recipients) {
            if (sender.sendMessage(recipient, "Batch notification", message)) {
                successful++;
            }
        }
        BatchSummary summary = new BatchSummary(recipients.size(), successful, recipients.size() - successful);
        System.out.println(summary);
        return summary;
    }
}
