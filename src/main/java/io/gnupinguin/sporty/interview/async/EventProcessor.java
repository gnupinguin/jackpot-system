package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.persistence.repository.BetRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotContributionRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessor {

    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository jackpotContributionRepository;

    @KafkaListener(topics = "jackpot-bets", containerFactory = "kafkaListenerContainerFactory")
    public void processEvent(BetEvent event) {
        log.info("Processing bet event: {}", event);
    }

}
