package io.gnupinguin.sporty.interview.processor.reward;


import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRuleType;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixedJackpotRuleRewarderTest {

    @Mock
    private Clock clock;

    @Mock
    private ChanceGenerator chanceGenerator;

    @Mock
    private JackpotRuleParamRepository ruleParamRepository;

    @InjectMocks
    private FixedJackpotRuleRewarder rewarder;

    private final Instant now = Instant.parse("2025-06-13T12:00:00Z");


    @Test
    void shouldReturnReward_whenChanceIsSuccessful() {
        when(clock.instant()).thenReturn(now);
        var jackpot = new Jackpot(1L, "Test", BigDecimal.TEN, new BigDecimal("1000.00"), 1L, 3L, now, now);
        var rule = new JackpotRule(3L, JackpotRuleType.REWARD, RuleStrategy.FIXED, "Fixed 10%", now);
        var bet = new Bet(99L, 42L, 1L, false, new BigDecimal("200.00"), now);

        when(ruleParamRepository.findParamsByRuleId(3L)).thenReturn(Map.of("chance", new BigDecimal("0.1")));
        when(chanceGenerator.won(any())).thenReturn(true);

        var reward = rewarder.reward(jackpot, rule, bet);
        assertNotNull(reward);
        assertEquals(bet.id(), reward.betId());
        assertEquals(bet.userId(), reward.userId());
        assertEquals(jackpot.id(), reward.jackpotId());
        assertEquals(jackpot.currentPoolAmount(), reward.rewardAmount());
        assertEquals(now, reward.createdAt());
    }

    @Test
    void shouldReturnNull_whenChanceFails() {
        var jackpot = new Jackpot(2L, "FailJackpot", BigDecimal.TEN, new BigDecimal("500.00"), 1L, 4L, now, now);
        var rule = new JackpotRule(4L, JackpotRuleType.REWARD, RuleStrategy.FIXED, "Low", now);
        var bet = new Bet(101L, 12L, 2L, false, new BigDecimal("50.00"), now);

        when(ruleParamRepository.findParamsByRuleId(4L)).thenReturn(Map.of("chance", new BigDecimal("0.05")));
        when(chanceGenerator.won(new BigDecimal("0.05"))).thenReturn(false);

        JackpotReward reward = rewarder.reward(jackpot, rule, bet);
        assertNull(reward);
    }

    @Test
    void shouldThrowIfChanceParamMissing() {
        Jackpot jackpot = new Jackpot(3L, "Broken", BigDecimal.TEN, new BigDecimal("999.00"), 1L, 5L, now, now);
        JackpotRule rule = new JackpotRule(5L, JackpotRuleType.REWARD, RuleStrategy.FIXED, "Invalid", now);
        Bet bet = new Bet(102L, 99L, 3L, false, new BigDecimal("10.00"), now);

        when(ruleParamRepository.findParamsByRuleId(5L)).thenReturn(Map.of());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> rewarder.reward(jackpot, rule, bet));
        assertTrue(ex.getMessage().contains("chance"));
    }

}