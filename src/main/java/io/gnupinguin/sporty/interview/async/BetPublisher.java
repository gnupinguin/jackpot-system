package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.rest.BetResource;
import jakarta.annotation.Nonnull;

public interface BetPublisher {

    void publishAsync(@Nonnull BetResource betResource);

}
