package io.gnupinguin.sporty.interview.processor.reward;

import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;

import static io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorHelper.requireParam;

@Service
@RequiredArgsConstructor
public class VariableJackpotRuleRewarder implements JackpotRuleRewarder {

    private final Clock clock;
    private final ChanceGenerator chanceGenerator;
    private final JackpotRuleParamRepository ruleParamRepository;


    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.VARIABLE;
    }

    @Nullable
    @Override
    public JackpotReward reward(@Nonnull JackpotRule rule, @Nonnull JackpotContribution contribution) {
        VariableRule ruleParams = VariableRule.fromParams(ruleParamRepository.findParamsByRuleId(rule.id()));

        var currentPool = contribution.jackpotPoolAfter();
        var triggerPool = ruleParams.triggerPool();
        var increaseRate = ruleParams.increaseRate();
        var maxChance = ruleParams.maxChance();

        BigDecimal chance;

        if (currentPool.compareTo(triggerPool) >= 0) {
            chance = maxChance;
        } else {
            chance = currentPool.multiply(increaseRate);
            if (chance.compareTo(maxChance) > 0) {
                chance = maxChance;
            }
        }

        if (!chanceGenerator.won(chance)) {
            return null;
        }

        return new JackpotReward(
                null,
                contribution.betId(),
                contribution.userId(),
                contribution.jackpotId(),
                currentPool,
                clock.instant()
        );
    }

    private record VariableRule(
            BigDecimal maxChance,
            BigDecimal increaseRate,
            BigDecimal triggerPool) {
        static VariableRule fromParams(Map<String, BigDecimal> params) {
            return new VariableRule(
                    requireParam(params, "max_chance"),
                    requireParam(params, "increase_rate"),
                    requireParam(params, "trigger_pool")
            );
        }
    }
}