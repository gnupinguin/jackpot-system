package io.gnupinguin.sporty.interview.rest;

import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("jackpot")
@RequiredArgsConstructor
public class JackpotEndpoint {

    private final JackpotRepository repository;

    @GetMapping
    public ResponseEntity<Jackpot> getJackpot(@RequestParam("id") Long id) {
        return ResponseEntity.of(repository.findById(id));
    }

}
