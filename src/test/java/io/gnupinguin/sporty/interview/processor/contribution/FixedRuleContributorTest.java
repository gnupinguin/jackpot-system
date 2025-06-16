package io.gnupinguin.sporty.interview.processor.contribution;


import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixedRuleContributorTest {

    @Mock
    private JackpotRuleParamRepository ruleParamRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private FixedRuleContributor contributor;

    private final Instant now = Instant.parse("2025-06-13T12:00:00Z");

    @Test
    void shouldCalculateContributionCorrectly() {
        when(clock.instant()).thenReturn(now);

        var jackpot = new Jackpot(1L, "Starter", BigDecimal.TEN, new BigDecimal("1000.00"),
                1L, 3L, now, 1);
        var rule = new JackpotRule(1L, JackpotRuleType.CONTRIBUTION, RuleStrategy.FIXED, "Fixed 5%", now);
        var bet = new Bet(101L, 1L, 1L, false, new BigDecimal("200.00"), now);

        when(ruleParamRepository.findParamsByRuleId(1L))
                .thenReturn(Map.of("rate", new BigDecimal("0.05")));

        var contribution = contributor.contribute(jackpot, rule, bet);

        assertNotNull(contribution);
        assertEquals(bet.id(), contribution.betId());
        assertEquals(bet.userId(), contribution.userId());
        assertEquals(jackpot.id(), contribution.jackpotId());
        assertEquals(bet.amount(), contribution.stakeAmount());
        assertEquals(new BigDecimal("10.0000"), contribution.contributionAmount());
        assertEquals(new BigDecimal("1010.0000"), contribution.jackpotPoolAfter());
        assertEquals(now, contribution.createdAt());
    }

    @Test
    void shouldThrowWhenRateMissing() {
        Jackpot jackpot = new Jackpot(1L, "Test", BigDecimal.TEN, BigDecimal.valueOf(1000), 1L, 3L, now, 1);
        JackpotRule rule = new JackpotRule(1L, JackpotRuleType.CONTRIBUTION, RuleStrategy.FIXED, "Broken", now);
        Bet bet = new Bet(1L, 1L, 1L, false, BigDecimal.valueOf(100), now);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> contributor.contribute(jackpot, rule, bet));

        assertTrue(exception.getMessage().contains("rate"));
    }

    @Test
    void shouldHandleZeroRate() {
        when(clock.instant()).thenReturn(now);

        Jackpot jackpot = new Jackpot(1L, "Jackpot", BigDecimal.TEN, BigDecimal.valueOf(999), 1L, 3L, now, 1);
        JackpotRule rule = new JackpotRule(1L, JackpotRuleType.CONTRIBUTION, RuleStrategy.FIXED, "Zero", now);
        Bet bet = new Bet(1L, 1L, 1L, false, BigDecimal.valueOf(500), now);

        when(ruleParamRepository.findParamsByRuleId(1L)).thenReturn(Map.of("rate", BigDecimal.ZERO));

        JackpotContribution contribution = contributor.contribute(jackpot, rule, bet);

        assertEquals(BigDecimal.ZERO, contribution.contributionAmount());
        assertEquals(new BigDecimal("999"), contribution.jackpotPoolAfter());
    }

}