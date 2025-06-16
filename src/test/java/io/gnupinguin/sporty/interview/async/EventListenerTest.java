package io.gnupinguin.sporty.interview.async;


import io.gnupinguin.sporty.interview.async.events.BetEvent;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRuleType;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRules;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.*;
import io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorProvider;
import io.gnupinguin.sporty.interview.processor.contribution.JackpotRuleContributor;
import io.gnupinguin.sporty.interview.processor.reward.JackpotRuleRewarder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock private BetRepository betRepository;
    @Mock private ExtendedJackpotRepository extendedJackpotRepository;
    @Mock private JackpotRepository jackpotRepository;
    @Mock private JackpotRuleRepository ruleRepository;
    @Mock private JackpotContributionRepository contributionRepository;
    @Mock private JackpotRuleProcessorProvider ruleProcessorProvider;
    @Mock private JackpotRewardRepository rewardRepository;
    @Mock private BetPublisher betPublisher;

    @InjectMocks
    private EventListener eventListener;

    @Mock private JackpotRuleContributor contributor;
    @Mock private JackpotRuleRewarder rewarder;

    private final Long betId = 1L;
    private final Long jackpotId = 2L;
    private final Long ruleId = 3L;

    private Bet bet;
    private Jackpot jackpot;
    private JackpotContribution contribution;
    private JackpotReward reward;
    private JackpotRules rules;

    @BeforeEach
    void setup() {
        bet = new Bet(betId, 100L, jackpotId, false, BigDecimal.valueOf(50), Instant.now());

        jackpot = new Jackpot(
                jackpotId,
                "Weekly Jackpot",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1000),
                ruleId,
                ruleId,
                Instant.now(),
                1L
        );

        contribution = new JackpotContribution(
                null,
                betId,
                bet.userId(),
                jackpotId,
                bet.amount(),
                BigDecimal.ONE,
                BigDecimal.valueOf(1001),
                Instant.now()
        );

        reward = new JackpotReward(
                null,
                betId,
                bet.userId(),
                jackpotId,
                BigDecimal.valueOf(1001),
                Instant.now()
        );

        var contributionRule = new JackpotRule(ruleId, JackpotRuleType.CONTRIBUTION, RuleStrategy.FIXED, "Fixed", Instant.now());
        var rewardRule = new JackpotRule(ruleId, JackpotRuleType.REWARD, RuleStrategy.FIXED, "Fixed", Instant.now());
        rules = new JackpotRules(contributionRule, rewardRule);
    }

    @Test
    void shouldProcessBetAndRewardSuccessfully() {
        when(betRepository.findById(betId)).thenReturn(Optional.of(bet));
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(jackpot));
        when(ruleRepository.findAllGroupedByType(List.of(ruleId, ruleId))).thenReturn(rules);
        when(ruleProcessorProvider.getContributor(RuleStrategy.FIXED)).thenReturn(contributor);
        when(contributor.contribute(jackpot, rules.contribution(), bet)).thenReturn(contribution);
        when(extendedJackpotRepository.incrementPool(contribution, jackpot.version())).thenReturn(true);
        when(ruleProcessorProvider.getRewarder(RuleStrategy.FIXED)).thenReturn(rewarder);
        when(rewarder.reward(rules.reward(), contribution)).thenReturn(reward);
        when(extendedJackpotRepository.resetJackpot(reward, jackpot.version())).thenReturn(true);

        eventListener.listenEvent(new BetEvent("", betId));

        verify(contributionRepository).save(contribution);
        verify(rewardRepository).save(reward);
        verify(betRepository).save(any());
        verifyNoMoreInteractions(betPublisher);
    }

    @Test
    void shouldSkipAlreadyProcessedBet() {
        Bet processed = new Bet(betId, 100L, jackpotId, true, null, null);
        when(betRepository.findById(betId)).thenReturn(Optional.of(processed));

        eventListener.listenEvent(new BetEvent("", betId));

        verifyNoInteractions(contributionRepository, rewardRepository, betPublisher);
    }

    @Test
    void shouldRetryWhenIncrementFails() {
        when(betRepository.findById(betId)).thenReturn(Optional.of(bet));
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(jackpot));
        when(ruleRepository.findAllGroupedByType(List.of(ruleId, ruleId))).thenReturn(rules);
        when(ruleProcessorProvider.getContributor(RuleStrategy.FIXED)).thenReturn(contributor);
        when(contributor.contribute(jackpot, rules.contribution(), bet)).thenReturn(contribution);
        when(extendedJackpotRepository.incrementPool(contribution, jackpot.version())).thenReturn(false);

        assertThrows(RetryKafkaEventException.class, () -> eventListener.listenEvent(new BetEvent("", betId)));
    }

    @Test
    void shouldSkipRewardIfNull() {
        when(betRepository.findById(betId)).thenReturn(Optional.of(bet));
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(jackpot));
        when(ruleRepository.findAllGroupedByType(List.of(ruleId, ruleId))).thenReturn(rules);
        when(ruleProcessorProvider.getContributor(RuleStrategy.FIXED)).thenReturn(contributor);
        when(contributor.contribute(jackpot, rules.contribution(), bet)).thenReturn(contribution);
        when(extendedJackpotRepository.incrementPool(contribution, jackpot.version())).thenReturn(true);
        when(ruleProcessorProvider.getRewarder(RuleStrategy.FIXED)).thenReturn(rewarder);
        when(rewarder.reward(rules.reward(), contribution)).thenReturn(null);

        eventListener.listenEvent(new BetEvent("", betId));

        verify(contributionRepository).save(contribution);
        verify(rewardRepository, never()).save(any());
    }

    @Test
    void shouldHandleUnknownExceptionAndTriggerRedelivery() {
        when(betRepository.findById(betId)).thenThrow(new RuntimeException("Database down"));

        eventListener.listenEvent(new BetEvent("", betId));

        verify(betPublisher).redelivery(eq(betId), contains("Database down"));
    }
}

