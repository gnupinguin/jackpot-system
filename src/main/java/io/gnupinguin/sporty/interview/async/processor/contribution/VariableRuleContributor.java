package io.gnupinguin.sporty.interview.async.processor.contribution;

import io.gnupinguin.sporty.interview.async.processor.AbstractJackpotRuleProcessor;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.Map;

@Service
public class VariableRuleContributor extends AbstractJackpotRuleProcessor implements JackpotRuleContributor {

    private final Clock clock;

    public VariableRuleContributor(JackpotRuleParamRepository ruleParamRepository, Clock clock) {
        super(ruleParamRepository);
        this.clock = clock;
    }

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.VARIABLE;
    }

    @Nonnull
    @Override
    public JackpotContribution contribute(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        var variableRule = loadRule(rule, VariableRule::fromParams);

        var currentPoolAmount = jackpot.currentPoolAmount();
        var steps = currentPoolAmount.divide(variableRule.decreaseStep(), 0, RoundingMode.DOWN);
        var rateDrop = variableRule.decreaseRate().multiply(steps);
        var effectiveRate = variableRule.initialRate().subtract(rateDrop);

        if (effectiveRate.compareTo(variableRule.minRate()) < 0) {
            effectiveRate = variableRule.minRate();
        }

        var contributionAmount = bet.amount().multiply(effectiveRate);
        var updatedJackpotPool = currentPoolAmount.add(contributionAmount);

        return new JackpotContribution(
                null,
                bet.id(),
                bet.userId(),
                jackpot.id(),
                bet.amount(),
                contributionAmount,
                updatedJackpotPool,
                clock.instant());
    }

    private record VariableRule(
            BigDecimal initialRate,
            BigDecimal minRate,
            BigDecimal decreaseStep,
            BigDecimal decreaseRate) {
        static VariableRule fromParams(Map<String, BigDecimal> params) {
            return new VariableRule(
                    requireParam(params, "initial_rate"),
                    requireParam(params, "min_rate"),
                    requireParam(params, "decrease_step"),
                    requireParam(params, "decrease_rate")
            );
        }

    }
}
