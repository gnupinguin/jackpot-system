package io.gnupinguin.sporty.interview.processor.contribution;

import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.Map;

import static io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorHelper.requireParam;

@Service
@RequiredArgsConstructor
public class VariableRuleContributor implements JackpotRuleContributor {

    private final Clock clock;
    private final JackpotRuleParamRepository ruleParamRepository;

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.VARIABLE;
    }

    @Nonnull
    @Override
    public JackpotContribution contribute(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        var variableRule = VariableRule.fromParams(ruleParamRepository.findParamsByRuleId(rule.id()));

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
