package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRules;
import io.gnupinguin.sporty.interview.persistence.repository.*;
import io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorProvider;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventListener {

    private final BetRepository betRepository;
    private final ExtendedJackpotRepository extendedJackpotRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotRuleRepository ruleRepository;
    private final JackpotContributionRepository contributionRepository;
    private final JackpotRuleProcessorProvider ruleProcessorProvider;
    private final JackpotRewardRepository rewardRepository;
    private final BetPublisher betPublisher;

    @KafkaListener(topics = "${kafka.jackpotBetsTopic}", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void listenEvent(BetEvent event) {
        try {
            log.info("Processing bet event: {}", event);
            var bet = betRepository.findById(event.betId()).orElseThrow(() -> new RuntimeException("Bet not found with ID: " + event.betId())); //TODO global error handling
            if (bet.processed()) {
                log.info("Bet with ID: {} is already processed. Skipping.", bet.id());
                return;
            }
            var jackpot = jackpotRepository.findById(bet.jackpotId()).orElseThrow(() -> new RuntimeException("Jackpot not found with ID: " + bet.jackpotId())); //TODO global error handling
            var rules = ruleRepository.findAllGroupedByType(List.of(jackpot.contributionRuleId(), jackpot.rewardRuleId()));

            var contribution = getContribution(rules, jackpot, bet);
            log.debug("Contributing to jackpot: {}", contribution);
            if (extendedJackpotRepository.incrementPool(contribution, jackpot.version())) {
                contributionRepository.save(contribution);
                saveReward(rules, contribution, jackpot.version());
                betRepository.save(bet.process());
            } else {
                log.info("Conflict while incrementing jackpot pool for bet with ID: {}, version: {}.", bet.id(), jackpot.version());
                throw new RetryKafkaEventException();
            }
        } catch (RetryKafkaEventException e) {
            throw e; // Allow retry
        } catch (Exception e) {
            log.error("Error processing bet event: {}", event, e);
            betPublisher.redelivery(event.betId(), e.getMessage());
        }
    }

    private JackpotContribution getContribution(JackpotRules rules, Jackpot jackpot, Bet bet) {
        var contributor = ruleProcessorProvider.getContributor(rules.contribution().strategy());
        return contributor.contribute(jackpot, rules.contribution(), bet);
    }

    private void saveReward(@Nonnull JackpotRules rules, @Nonnull JackpotContribution contribution, long currentVersion) {
        var rewarder = ruleProcessorProvider.getRewarder(rules.reward().strategy());
        var reward = rewarder.reward(rules.reward(), contribution);
        log.info("Rewarded bet with ID: {}. Reward: {}", contribution.betId(), reward);
        if (reward != null) {
            if (extendedJackpotRepository.resetJackpot(reward, currentVersion)) {
                rewardRepository.save(reward);
            } else {
                log.info("Conflict while resetting jackpot for bet with ID: {}. Skip", contribution.betId());
            }
        } else {
            log.debug("No reward for bet with ID: {}", contribution.betId());
        }
    }

}
