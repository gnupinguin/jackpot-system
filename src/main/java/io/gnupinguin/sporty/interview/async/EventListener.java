package io.gnupinguin.sporty.interview.async;

import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.persistence.repository.*;
import io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorProvider;
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
    private final ExtendedJackpotRepository jackpotRepository;
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
            var jackpot = jackpotRepository.findForUpdate(bet.jackpotId()).orElseThrow(() -> new RuntimeException("Jackpot not found with ID: " + bet.jackpotId())); //TODO global error handling

            var rules = ruleRepository.findAllGroupedByType(List.of(jackpot.contributionRuleId(), jackpot.rewardRuleId()));

            var contributor = ruleProcessorProvider.getContributor(rules.contribution().strategy());
            var contribution = contributor.contribute(jackpot, rules.contribution(), bet);
            log.info("Contributing to jackpot: {}", contribution);
            contributionRepository.save(contribution);
            jackpot = jackpot.updatePool(contribution.jackpotPoolAfter(), contribution.createdAt());

            var rewarder = ruleProcessorProvider.getRewarder(rules.reward().strategy());
            var reward = rewarder.reward(jackpot, rules.reward(), bet);
            log.info("Rewarded bet with ID: {}. Reward: {}", bet.id(), reward);
            if (reward != null) {
                rewardRepository.save(reward);
                jackpotRepository.update(jackpot.resetPool(reward.createdAt()));
            } else {
                log.info("No reward for bet with ID: {}", bet.id());
            }
            betRepository.save(bet.process());
        } catch (Exception e) {
            log.error("Error processing bet event: {}", event, e);
            betPublisher.redelivery(event.betId(), e.getMessage());
        }
    }

}
