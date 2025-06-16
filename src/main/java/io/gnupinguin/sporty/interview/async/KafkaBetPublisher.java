package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.config.KafkaProperties;
import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaBetPublisher implements BetPublisher {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, Object> producer;

    @Override
    public void publishAsync(@Nonnull Bet bet) {
        var event = new BetEvent(getEventId(), bet.id());
        producer.send(kafkaProperties.jackpotBetsTopic(), String.valueOf(bet.jackpotId()), event);
    }

    @Override
    public void redelivery(long betId, @Nonnull String reason) {
        //TODO
        log.info("[MOCK] Redelivering bet with ID: {}. Reason: {}", betId, reason);
    }

    @Nonnull
    private static String getEventId() {
        return UUID.randomUUID().toString();
    }

}
