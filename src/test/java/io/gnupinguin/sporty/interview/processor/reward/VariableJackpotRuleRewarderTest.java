package io.gnupinguin.sporty.interview.processor.reward;

import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VariableJackpotRuleRewarderTest {

    @Mock
    private Clock clock;

    @Mock
    private ChanceGenerator chanceGenerator;

    @Mock
    private JackpotRuleParamRepository ruleParamRepository;

    @InjectMocks
    private VariableJackpotRuleRewarder rewarder;

    @Test
    void getStrategy_returns_VARIABLE() {
        assertEquals(RuleStrategy.VARIABLE, rewarder.getStrategy());
    }

    @Test
    void reward_returnsNull_whenChanceGeneratorSaysNo() {
        var jackpot = mock(Jackpot.class);
        var rule = mock(JackpotRule.class);
        var bet = mock(Bet.class);

        when(jackpot.currentPoolAmount()).thenReturn(new BigDecimal("50"));
        when(rule.id()).thenReturn(1L);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of(
                "max_chance", new BigDecimal("0.1"),
                "increase_rate", new BigDecimal("0.002"),
                "trigger_pool", new BigDecimal("100")
        ));

        when(chanceGenerator.won(any())).thenReturn(false);

        assertNull(rewarder.reward(jackpot, rule, bet));
    }

    @Test
    void reward_returnsJackpotReward_whenChanceGeneratorSaysYes() {
        var jackpot = mock(Jackpot.class);
        var rule = mock(JackpotRule.class);
        var bet = mock(Bet.class);

        when(jackpot.currentPoolAmount()).thenReturn(new BigDecimal("50"));
        when(jackpot.id()).thenReturn(100L);
        when(rule.id()).thenReturn(1L);
        when(bet.id()).thenReturn(200L);
        when(bet.userId()).thenReturn(300L);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of(
                "max_chance", new BigDecimal("0.1"),
                "increase_rate", new BigDecimal("0.002"),
                "trigger_pool", new BigDecimal("100")
        ));

        when(chanceGenerator.won(new BigDecimal("0.100"))).thenReturn(true);

        Instant now = Instant.parse("2025-06-13T10:15:30.00Z");
        when(clock.instant()).thenReturn(now);

        var reward = rewarder.reward(jackpot, rule, bet);
        assertNotNull(reward);
        assertEquals(200L, reward.betId());
        assertEquals(300L, reward.userId());
        assertEquals(100L, reward.jackpotId());
        assertEquals(new BigDecimal("50"), reward.rewardAmount());
        assertEquals(now, reward.createdAt());
    }

    @Test
    void reward_calculatesChanceCorrectly_whenCurrentPoolLessThanTriggerPool() {
        var jackpot = mock(Jackpot.class);
        var rule = mock(JackpotRule.class);
        var bet = mock(Bet.class);

        var currentPool = new BigDecimal("40");
        var triggerPool = new BigDecimal("100");
        var increaseRate = new BigDecimal("0.002");
        var maxChance = new BigDecimal("0.1");

        when(jackpot.currentPoolAmount()).thenReturn(currentPool);
        when(rule.id()).thenReturn(1L);
        when(bet.id()).thenReturn(10L);
        when(bet.userId()).thenReturn(20L);
        when(jackpot.id()).thenReturn(30L);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of(
                "max_chance", maxChance,
                "increase_rate", increaseRate,
                "trigger_pool", triggerPool
        ));

        var expectedChance = currentPool.multiply(increaseRate); // 40 * 0.002 = 0.08

        when(chanceGenerator.won(expectedChance)).thenReturn(true);

        var reward = rewarder.reward(jackpot, rule, bet);
        assertNotNull(reward);
    }

    @Test
    void reward_capsChanceAtMaxChance_whenCurrentPoolTimesIncreaseRateExceedsMaxChance() {
        Jackpot jackpot = mock(Jackpot.class);
        JackpotRule rule = mock(JackpotRule.class);
        Bet bet = mock(Bet.class);

        var currentPool = new BigDecimal("1000");
        var triggerPool = new BigDecimal("2000");
        var increaseRate = new BigDecimal("0.01");
        var maxChance = new BigDecimal("0.1");

        when(jackpot.currentPoolAmount()).thenReturn(currentPool);
        when(rule.id()).thenReturn(1L);
        when(bet.id()).thenReturn(10L);
        when(bet.userId()).thenReturn(20L);
        when(jackpot.id()).thenReturn(30L);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of(
                "max_chance", maxChance,
                "increase_rate", increaseRate,
                "trigger_pool", triggerPool
        ));

        // currentPool * increaseRate = 1000 * 0.01 = 10.0 which is > maxChance(0.1)
        // so expected chance is maxChance (0.1)
        when(chanceGenerator.won(maxChance)).thenReturn(true);

        var reward = rewarder.reward(jackpot, rule, bet);
        assertNotNull(reward);
    }

    @Test
    void reward_setsChanceToMaxChance_whenCurrentPoolEqualsOrExceedsTriggerPool() {
        Jackpot jackpot = mock(Jackpot.class);
        JackpotRule rule = mock(JackpotRule.class);
        Bet bet = mock(Bet.class);

        var currentPool = new BigDecimal("150");
        var triggerPool = new BigDecimal("100");
        var increaseRate = new BigDecimal("0.01");
        var maxChance = new BigDecimal("0.2");

        when(jackpot.currentPoolAmount()).thenReturn(currentPool);
        when(rule.id()).thenReturn(1L);
        when(bet.id()).thenReturn(10L);
        when(bet.userId()).thenReturn(20L);
        when(jackpot.id()).thenReturn(30L);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of(
                "max_chance", maxChance,
                "increase_rate", increaseRate,
                "trigger_pool", triggerPool
        ));

        // currentPool >= triggerPool, chance = maxChance
        when(chanceGenerator.won(maxChance)).thenReturn(true);

        var reward = rewarder.reward(jackpot, rule, bet);
        assertNotNull(reward);
    }

}
