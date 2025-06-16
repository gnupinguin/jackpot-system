package io.gnupinguin.sporty.interview.async.task;

import jakarta.annotation.Nonnull;

public interface BetTaskQueue {
    void submit(@Nonnull BetTask task);
}
