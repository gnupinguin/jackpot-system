package io.gnupinguin.sporty.interview.processor.reward;


import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        var rule = new JackpotRule(3L, JackpotRuleType.REWARD, RuleStrategy.FIXED, "Fixed 10%", now);
        var contribution = new JackpotContribution(1L, 2L, 3L, 4L, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, now);
        when(ruleParamRepository.findParamsByRuleId(3L)).thenReturn(Map.of("chance", new BigDecimal("0.1")));
        when(chanceGenerator.won(any())).thenReturn(true);

        var reward = rewarder.reward(rule, contribution);
        assertNotNull(reward);
        assertEquals(contribution.betId(), reward.betId());
        assertEquals(contribution.userId(), reward.userId());
        assertEquals(contribution.jackpotId(), reward.jackpotId());
        assertEquals(contribution.jackpotPoolAfter(), reward.rewardAmount());
        assertEquals(now, reward.createdAt());
    }

}