package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.persistence.model.Bet;
import jakarta.annotation.Nonnull;

public interface BetPublisher {

    void publishAsync(@Nonnull Bet bet);

    void redelivery(long betId, @Nonnull String reason);
}
