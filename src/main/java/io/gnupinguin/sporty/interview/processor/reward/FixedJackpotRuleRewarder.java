package io.gnupinguin.sporty.interview.processor.reward;

import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorHelper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class FixedJackpotRuleRewarder implements JackpotRuleRewarder {

    private final Clock clock;
    private final ChanceGenerator chanceGenerator;
    private final JackpotRuleParamRepository ruleParamRepository;

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.FIXED;
    }

    @Nullable
    @Override
    public JackpotReward reward(@Nonnull JackpotRule rule, @Nonnull JackpotContribution contribution) {
        var fixedRule = FixedRule.fromParams(ruleParamRepository.findParamsByRuleId(rule.id()));

        if (!chanceGenerator.won(fixedRule.chance)) {
            return null;
        }

        return new JackpotReward(
                null, // ID (to be generated)
                contribution.betId(),
                contribution.userId(),
                contribution.jackpotId(),
                contribution.jackpotPoolAfter(),
                clock.instant()
        );
    }

    private record FixedRule(BigDecimal chance) {
        static FixedRule fromParams(Map<String, BigDecimal> params) {
            return new FixedRule(JackpotRuleProcessorHelper.requireParam(params, "chance"));
        }
    }

}
