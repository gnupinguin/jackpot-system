package io.gnupinguin.sporty.interview.processor.reward;

import io.gnupinguin.sporty.interview.common.ChanceGenerator;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
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
    public JackpotReward reward(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        var fixedRule = FixedRule.fromParams(ruleParamRepository.findParamsByRuleId(rule.id()));

        if (!chanceGenerator.won(fixedRule.chance)) {
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
            return new FixedRule(JackpotRuleProcessorHelper.requireParam(params, "chance"));
        }
    }

}
