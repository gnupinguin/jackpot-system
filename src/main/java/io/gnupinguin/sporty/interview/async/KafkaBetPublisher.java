package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.rest.BetResource;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

@Service
public class KafkaBetPublisher implements BetPublisher {
    @Override
    public void publishAsync(@Nonnull BetResource betResource) {

    }
}
