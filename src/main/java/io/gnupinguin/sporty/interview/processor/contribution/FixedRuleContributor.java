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
import java.time.Clock;
import java.util.Map;

import static io.gnupinguin.sporty.interview.processor.JackpotRuleProcessorHelper.requireParam;

@Service
@RequiredArgsConstructor
public class FixedRuleContributor implements JackpotRuleContributor {

    private final Clock clock;
    private final JackpotRuleParamRepository ruleParamRepository;

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.FIXED;
    }

    @Nonnull
    @Override
    public JackpotContribution contribute(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        var fixedRule = FixedRule.fromParams(ruleParamRepository.findParamsByRuleId(rule.id()));
        var contributionAmount = bet.amount().multiply(fixedRule.rate());
        var updatedJackpotPool = jackpot.currentPoolAmount().add(contributionAmount);

        return new JackpotContribution(
                null,
                bet.id(),
                bet.userId(),
                jackpot.id(),
                bet.amount(),
                contributionAmount,
                updatedJackpotPool,
                clock.instant()
        );
    }

    private record FixedRule(BigDecimal rate) {
        static FixedRule fromParams(Map<String, BigDecimal> params) {
            return new FixedRule(requireParam(params, "rate"));
        }
    }
}
