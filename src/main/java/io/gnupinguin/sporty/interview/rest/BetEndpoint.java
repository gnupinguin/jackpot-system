package io.gnupinguin.sporty.interview.rest;

import io.gnupinguin.sporty.interview.async.BetPublisher;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.repository.BetRepository;
import io.gnupinguin.sporty.interview.persistence.repository.ExtendedJackpotRepository;
import io.gnupinguin.sporty.interview.rest.resource.BetRequest;
import io.gnupinguin.sporty.interview.rest.resource.BetResource;
import io.gnupinguin.sporty.interview.rest.resource.RewardResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;


@Slf4j
@RestController
@RequestMapping("bet")
@RequiredArgsConstructor
public class BetEndpoint {

    private final Clock clock;
    private final BetRepository betRepository;
    private final ExtendedJackpotRepository jackpotRepository;
    private final BetPublisher publisher;

    @PostMapping("place")
    @Transactional
    public ResponseEntity<BetResource> place(@RequestBody BetRequest request) {
        //TODO: validate request
        log.info("Place bet request: {}", request);
        try {
            var bet = betRepository.save(new Bet(null, request.userId(), request.jackpotId(), false, request.amount(), clock.instant()));
            publisher.publishAsync(bet);
            return ResponseEntity.ok(new BetResource(bet.id(), bet.userId(), bet.jackpotId(), bet.amount()));
        } catch (Exception e) {
            log.error("Error placing bet", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("check-reward")
    public ResponseEntity<RewardResource> check(@RequestParam("betId") Long betId) {
        log.info("Check reward for bet with ID: {}", betId);
        var rewardResource = jackpotRepository.findRewardedBetByBetId(betId)
                .map(r -> {
                    if (r.processed()) {
                        if (r.rewardId() != null) {
                            return RewardResource.won(r.rewardAmount());
                        } else {
                            return RewardResource.loose();
                        }
                    }
                    return RewardResource.notProcessed();
                });
        return ResponseEntity.of(rewardResource);
    }

}
