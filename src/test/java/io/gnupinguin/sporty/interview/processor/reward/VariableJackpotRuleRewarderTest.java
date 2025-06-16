package io.gnupinguin.sporty.interview.processor.reward;

import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariableJackpotRuleRewarderTest {

    @Mock
    private JackpotRuleParamRepository ruleParamRepository;

    @Mock
    private ChanceGenerator chanceGenerator;

    private Clock clock;

    @InjectMocks
    private VariableJackpotRuleRewarder rewarder;

    private final Instant now = Instant.parse("2023-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(now, ZoneOffset.UTC);
        rewarder = new VariableJackpotRuleRewarder(clock, chanceGenerator, ruleParamRepository);
    }

    @Test
    void testGetStrategy() {
        assertEquals(RuleStrategy.VARIABLE, rewarder.getStrategy());
    }

    @Test
    void testReward_ChanceClampedToMax() {
        JackpotRule rule = mock(JackpotRule.class);
        when(rule.id()).thenReturn(3L);
        when(ruleParamRepository.findParamsByRuleId(3L)).thenReturn(Map.of(
                "max_chance", BigDecimal.valueOf(0.5),
                "increase_rate", BigDecimal.valueOf(0.01),
                "trigger_pool", BigDecimal.valueOf(300)
        ));

        JackpotContribution contribution = new JackpotContribution(
                1L, 100L, 200L, 300L,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(1000), // 1000 * 0.01 = 10 > maxChance
                now
        );

        when(chanceGenerator.won(BigDecimal.valueOf(0.5))).thenReturn(true);

        var reward = rewarder.reward(rule, contribution);

        assertNotNull(reward);
        assertEquals(BigDecimal.valueOf(1000), reward.rewardAmount());
    }

    @Test
    void testReward_UsesMaxChance_WhenTriggerReached() {
        JackpotRule rule = mock(JackpotRule.class);
        when(rule.id()).thenReturn(4L);
        when(ruleParamRepository.findParamsByRuleId(4L)).thenReturn(Map.of(
                "max_chance", BigDecimal.valueOf(0.4),
                "increase_rate", BigDecimal.valueOf(0.02),
                "trigger_pool", BigDecimal.valueOf(500)
        ));

        JackpotContribution contribution = new JackpotContribution(
                1L, 100L, 200L, 300L,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(600), // >= trigger_pool
                now
        );

        when(chanceGenerator.won(BigDecimal.valueOf(0.4))).thenReturn(true);

        var reward = rewarder.reward(rule, contribution);

        assertNotNull(reward);
        assertEquals(BigDecimal.valueOf(600), reward.rewardAmount());
    }
}

