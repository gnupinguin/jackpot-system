package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.async.processor.JackpotRuleContributorProvider;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.repository.BetRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotContributionRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventListener {

    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotRuleRepository jackpotRuleRepository;
    private final JackpotContributionRepository jackpotContributionRepository;
    private final JackpotRuleContributorProvider ruleContributorProvider;
    private final JdbcTemplate jdbcTemplate;
    private final BetPublisher betPublisher;

    @KafkaListener(topics = "jackpot-bets", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listenEvent(BetEvent event) {
        try {
            log.info("Processing bet event: {}", event);
            var bet = fetch(event.betId(), betRepository);
            var jackpot = fetch(bet.jackpotId(), jackpotRepository);
            var jackpotRule = fetch(jackpot.contributionRuleId(), jackpotRuleRepository);
            var contributor = ruleContributorProvider.getContributor(jackpotRule.strategy());
            var contribution = contributor.contribute(jackpot, jackpotRule, bet);
            log.info("Contributing to jackpot: {}", contribution);
            jackpotContributionRepository.save(contribution);

            incrementJackpotAmount(jackpot.id(), contribution.contributionAmount(), contribution.createdAt());
        } catch (Exception e) {
            log.error("Error processing bet event: {}", event, e);
            betPublisher.redelivery(event.betId(), e.getMessage());
        }
    }

    private <T> T fetch(Long id, CrudRepository<T, Long> repository) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Entity not found by id "+ id + " in repository " + repository.getClass().getSimpleName()));
    }

    private void incrementJackpotAmount(long jackpotId, @Nonnull BigDecimal delta, @Nonnull Instant updatedAt) {
        jdbcTemplate.update(JackpotRepository.INCREMENT_JACKPOT_POOL_QUERY, delta, updatedAt, jackpotId);
    }

}
