package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.async.events.BetRedeliveryEvent;
import io.gnupinguin.sporty.interview.async.kafka.KafkaProperties;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaBetPublisher implements BetPublisher {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, Object> producer;

    @Override
    public void publishAsync(@Nonnull Bet bet) {
        producer.send(kafkaProperties.jackpotBetsTopic(), new BetEvent(getEventId(), bet.id()));
    }

    @Override
    public void redelivery(long betId, @Nonnull String reason) {
        producer.send(kafkaProperties.jackpotBetsRedeliveryTopic(), new BetRedeliveryEvent(getEventId(), betId, reason));
    }

    @Nonnull
    private static String getEventId() {
        return UUID.randomUUID().toString();
    }

}
