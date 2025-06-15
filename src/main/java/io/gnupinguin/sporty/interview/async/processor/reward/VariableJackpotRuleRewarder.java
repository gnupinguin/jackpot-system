package io.gnupinguin.sporty.interview.async.processor.reward;

import io.gnupinguin.sporty.interview.async.processor.AbstractJackpotRuleProcessor;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Random;

@Service
public class VariableJackpotRuleRewarder extends AbstractJackpotRuleProcessor implements JackpotRuleRewarder {

    private final Clock clock;
    private static final Random random = new SecureRandom();

    public VariableJackpotRuleRewarder(JackpotRuleParamRepository ruleParamRepository, Clock clock) {
        super(ruleParamRepository);
        this.clock = clock;
    }

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.VARIABLE;
    }

    @Nullable
    @Override
    public JackpotReward reward(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        VariableRule ruleParams = loadRule(rule, VariableRule::fromParams);

        BigDecimal currentPool = jackpot.currentPoolAmount();
        BigDecimal triggerPool = ruleParams.triggerPool();
        BigDecimal increaseRate = ruleParams.increaseRate();
        BigDecimal maxChance = ruleParams.maxChance();

        BigDecimal chance;

        if (currentPool.compareTo(triggerPool) >= 0) {
            chance = maxChance;
        } else {
            chance = currentPool.multiply(increaseRate);
            if (chance.compareTo(maxChance) > 0) {
                chance = maxChance;
            }
        }

        if (random.nextDouble() > chance.doubleValue()) {
            return null;
        }

        return new JackpotReward(
                null,
                bet.id(),
                bet.userId(),
                jackpot.id(),
                currentPool,
                Instant.now(clock)
        );
    }

    private record VariableRule(
            BigDecimal maxChance,
            BigDecimal increaseRate,
            BigDecimal triggerPool
    ) {
        static VariableRule fromParams(Map<String, BigDecimal> params) {
            return new VariableRule(
                    requireParam(params, "max_chance"),
                    requireParam(params, "increase_rate"),
                    requireParam(params, "trigger_pool")
            );
        }
    }
}