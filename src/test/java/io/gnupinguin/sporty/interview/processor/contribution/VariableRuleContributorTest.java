package io.gnupinguin.sporty.interview.processor.contribution;


import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariableRuleContributorTest {

    @Mock
    private Clock clock;

    @Mock
    private JackpotRuleParamRepository ruleParamRepository;

    @InjectMocks
    private VariableRuleContributor contributor;

    private final Instant now = Instant.parse("2025-06-13T12:00:00Z");

    @Test
    void shouldCalculateContribution_withinRateBounds() {
        when(clock.instant()).thenReturn(now);

        var jackpot = new Jackpot(1L, "Mega", BigDecimal.TEN, new BigDecimal("3000.00"), 2L, 4L, now, now);
        var rule = new JackpotRule(2L, JackpotRuleType.CONTRIBUTION, RuleStrategy.VARIABLE, "Variable Rate", now);
        var bet = new Bet(1L, 1L, 1L, false, new BigDecimal("100.00"), now);

        // Pool = 3000, step = 1000 → steps = 3
        // Drop = 0.005 * 3 = 0.015 → rate = 0.10 - 0.015 = 0.085
        // Contribution = 100 * 0.085 = 8.5
        Map<String, BigDecimal> params = Map.of(
                "initial_rate", new BigDecimal("0.10"),
                "min_rate", new BigDecimal("0.02"),
                "decrease_step", new BigDecimal("1000"),
                "decrease_rate", new BigDecimal("0.005")
        );

        when(ruleParamRepository.findParamsByRuleId(2L)).thenReturn(params);

        var contribution = contributor.contribute(jackpot, rule, bet);
        assertNotNull(contribution);
        assertEquals(new BigDecimal("8.50000"), contribution.contributionAmount());
        assertEquals(new BigDecimal("3008.50000"), contribution.jackpotPoolAfter());
        assertEquals(now, contribution.createdAt());
    }

    @Test
    void shouldUseMinRate_whenDropTooBig() {
        when(clock.instant()).thenReturn(now);

        var jackpot = new Jackpot(1L, "Mini", BigDecimal.TEN, new BigDecimal("10000.00"), 2L, 4L, now, now);
        var rule = new JackpotRule(2L, JackpotRuleType.CONTRIBUTION, RuleStrategy.VARIABLE, "Rate Floor", now);
        Bet bet = new Bet(1L, 1L, 1L, false, new BigDecimal("50.00"), now);

        // Drop too large, effective rate < min → fallback to min_rate (0.02)
        Map<String, BigDecimal> params = Map.of(
                "initial_rate", new BigDecimal("0.10"),
                "min_rate", new BigDecimal("0.02"),
                "decrease_step", new BigDecimal("1000"),
                "decrease_rate", new BigDecimal("0.02")
        );

        when(ruleParamRepository.findParamsByRuleId(2L)).thenReturn(params);

        var contribution = contributor.contribute(jackpot, rule, bet);
        assertEquals(new BigDecimal("1.0000"), contribution.contributionAmount()); // 50 * 0.02
        assertEquals(new BigDecimal("10001.0000"), contribution.jackpotPoolAfter());
    }

    @Test
    void shouldThrow_whenParameterMissing() {
        var jackpot = new Jackpot(1L, "Broken", BigDecimal.TEN, new BigDecimal("500.00"), 2L, 4L, now, now);
        var rule = new JackpotRule(2L, JackpotRuleType.CONTRIBUTION, RuleStrategy.VARIABLE, "Incomplete", now);
        var bet = new Bet(1L, 1L, 1L, false, new BigDecimal("200.00"), now);

        when(ruleParamRepository.findParamsByRuleId(2L)).thenReturn(Map.of(
                "initial_rate", BigDecimal.ONE // other required keys are missing
        ));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> contributor.contribute(jackpot, rule, bet));

        assertTrue(ex.getMessage().contains("min_rate"));
    }

}