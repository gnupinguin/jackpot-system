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
import java.util.Map;
import java.util.Random;

@Service
public class FixedJackpotRuleRewarder extends AbstractJackpotRuleProcessor implements JackpotRuleRewarder {

    private static final Random random = new SecureRandom(); // use thread-safe or inject for testability

    private final Clock clock; // use system clock or inject for testability

    public FixedJackpotRuleRewarder(JackpotRuleParamRepository ruleParamRepository, Clock clock, JackpotRuleParamRepository ruleParamRepository1) {
        super(ruleParamRepository);
        this.clock = clock;
    }

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.FIXED;
    }

    @Nullable
    @Override
    public JackpotReward reward(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        FixedRule fixedRule = loadRule(rule, FixedRule::fromParams);

        if (random.nextDouble() > fixedRule.chance().doubleValue()) {
            return null;
        }

        BigDecimal rewardAmount = jackpot.currentPoolAmount();

        return new JackpotReward(
                null, // ID (to be generated)
                bet.id(),
                bet.userId(),
                jackpot.id(),
                rewardAmount,
                clock.instant()
        );
    }

    private record FixedRule(BigDecimal chance) {
        static FixedRule fromParams(Map<String, BigDecimal> params) {
            BigDecimal chance = params.get("chance");
            if (chance == null) {
                throw new IllegalArgumentException("Missing required parameter 'chance' for FIXED reward rule");
            }
            return new FixedRule(chance);
        }
    }
}
