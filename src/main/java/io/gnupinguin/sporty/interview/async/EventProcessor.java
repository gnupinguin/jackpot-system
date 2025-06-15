package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.persistence.repository.BetRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotContributionRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessor {

    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotContributionRepository jackpotContributionRepository;

    public void processEvent(@Nonnull BetEvent event) {
        //todo check user id
        log.info("Processing bet event: {}", event);
//        var jackpotHolder = jackpotRepository.findById(event.jackpotId());
//        if (jackpotHolder.isPresent()) {
//            var jackpot = jackpotHolder.get();
//
//        } else {
//            log.warn("Jackpot with id {} not found", event.jackpotId());
//        }


    }

}
