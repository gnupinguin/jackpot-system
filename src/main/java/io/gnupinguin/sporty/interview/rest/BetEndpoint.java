package io.gnupinguin.sporty.interview.rest;
import io.gnupinguin.sporty.interview.async.BetPublisher;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.repository.BetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;


@Slf4j
@RestController
@RequestMapping("bet")
@RequiredArgsConstructor
public class BetEndpoint {

    private final Clock clock;
    private final BetRepository repository;
    private final BetPublisher publisher;

    @PostMapping
    @Transactional
    public Bet place(@RequestBody BetResource request) {
        log.info("Place bet request: {}", request);
        try {
            var bet = repository.save(new Bet(null, request.userId(), request.jackpotId(), request.amount(), clock.instant()));
            publisher.publishAsync(bet);
            return bet;
        } catch (Exception e) {
            log.error("Error placing bet", e);
            throw new RuntimeException("Failed to place bet", e); //TODO global error handling
        }
    }

}
