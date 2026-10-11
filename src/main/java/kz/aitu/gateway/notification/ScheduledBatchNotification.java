package kz.aitu.gateway.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import kz.aitu.gateway.sender.MessageSender;

public final class ScheduledBatchNotification extends Notification {
    private final List<String> recipients;

    public ScheduledBatchNotification(MessageSender sender, Collection<String> recipients) {
        super(sender);
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
