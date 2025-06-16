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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetEventProcessorTest {

    @Mock private BetRepository betRepository;
    @Mock private JackpotRepository jackpotRepository;
    @Mock private JackpotRuleRepository ruleRepository;
    @Mock private JackpotContributionRepository contributionRepository;
    @Mock private JackpotRuleProcessorProvider ruleProcessorProvider;
    @Mock private JackpotRewardRepository rewardRepository;

    @Mock private JackpotRuleContributor contributor;
    @Mock private JackpotRuleRewarder rewarder;

    @InjectMocks private BetEventProcessor listener;

    private final Instant now = Instant.parse("2025-06-13T12:00:00Z");

    private Bet bet;
    private Jackpot jackpot;
    private JackpotRule contributionRule;
    private JackpotRule rewardRule;

    @BeforeEach
    void setup() {
        bet = new Bet(1L, 10L, 100L, false, new BigDecimal("500.00"), now);
        jackpot = new Jackpot(100L, "Starter", new BigDecimal("1000"), new BigDecimal("1000"), 1L, 2L, now, now);
        contributionRule = new JackpotRule(1L, JackpotRuleType.CONTRIBUTION, RuleStrategy.FIXED, "Fixed 5%", now);
        rewardRule = new JackpotRule(2L, JackpotRuleType.REWARD, RuleStrategy.FIXED, "Fixed Reward", now);
    }

    @Test
    void testProcessContributionAndReward() {
        BetEvent event = new BetEvent("uuid", bet.id(), jackpot.id());

        when(betRepository.findById(bet.id())).thenReturn(Optional.of(bet));
        when(jackpotRepository.findById(bet.jackpotId())).thenReturn(Optional.of(jackpot));
        when(ruleRepository.findAllGroupedByType(List.of(1L, 2L)))
                .thenReturn(new JackpotRules(contributionRule, rewardRule));

        when(ruleProcessorProvider.getContributor(RuleStrategy.FIXED)).thenReturn(contributor);
        when(contributor.contribute(jackpot, contributionRule, bet))
                .thenReturn(new JackpotContribution(null, bet.id(), bet.userId(), jackpot.id(),
                        bet.amount(), new BigDecimal("25.00"), new BigDecimal("1025.00"), now));

        when(ruleProcessorProvider.getRewarder(RuleStrategy.FIXED)).thenReturn(rewarder);
        when(rewarder.reward(any(), any(), any()))
                .thenReturn(new JackpotReward(null, bet.id(), bet.userId(), jackpot.id(), new BigDecimal("200.00"), now));

        listener.process(event);

        verify(contributionRepository).save(any());
        verify(rewardRepository).save(any());
        verify(jackpotRepository).save(any());
        verify(betRepository).save(argThat(Bet::processed));
    }

    @Test
    void testIgnoreAlreadyProcessedBet() {
        BetEvent event = new BetEvent("uuid", bet.id(), jackpot.id());
        bet = bet.process();

        when(betRepository.findById(bet.id())).thenReturn(Optional.of(bet));

        listener.process(event);

        verify(betRepository, never()).save(any());
        verifyNoInteractions(jackpotRepository, ruleRepository, contributionRepository, rewardRepository, ruleProcessorProvider);
    }

}
