package io.gnupinguin.sporty.interview.rest;
import io.gnupinguin.sporty.interview.async.BetPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/bet")
@RequiredArgsConstructor
public class BetEndpoint {

    private final BetPublisher betPublisher;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void make(@RequestBody BetResource request) {
        log.info("Publishing bet request: {}", request);
        betPublisher.publishAsync(request);
    }

}
