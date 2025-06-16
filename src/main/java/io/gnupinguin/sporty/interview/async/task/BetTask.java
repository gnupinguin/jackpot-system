package io.gnupinguin.sporty.interview.async.task;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import jakarta.annotation.Nonnull;
import org.springframework.kafka.support.Acknowledgment;

public record BetTask(BetEvent event, Acknowledgment ack, int retries, Runnable processor) {

    public BetTask(BetEvent event, Acknowledgment ack, Runnable processor) {
        this(event, ack, 0, processor);
    }

    @Nonnull
    BetTask nextAttempt() {
        return new BetTask(event, ack, retries + 1, processor);
    }

}
