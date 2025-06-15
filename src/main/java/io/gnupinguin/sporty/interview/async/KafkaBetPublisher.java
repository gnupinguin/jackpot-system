package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.rest.BetResource;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaBetPublisher implements BetPublisher {

    private final KafkaTemplate<String, Object> producer;

    @Override
    public void publishAsync(@Nonnull Bet bet) {
        //TODO extract topic to configuration
        producer.send("jackpot-bets", new BetEvent(UUID.randomUUID().toString(), bet));
    }


}
