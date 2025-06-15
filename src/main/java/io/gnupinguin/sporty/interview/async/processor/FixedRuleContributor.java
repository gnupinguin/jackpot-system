package io.gnupinguin.sporty.interview.async.processor;

import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;

@Service
public class FixedRuleContributor extends AbstractJackpotRuleContributor {

    private final Clock clock;

    public FixedRuleContributor(JackpotRuleParamRepository ruleParamRepository, Clock clock) {
        super(ruleParamRepository);
        this.clock = clock;
    }

    @Nonnull
    @Override
    public RuleStrategy getStrategy() {
        return RuleStrategy.FIXED;
    }

    @Nonnull
    @Override
    public JackpotContribution contribute(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet) {
        var fixedRule = loadRule(rule, FixedRule::fromParams);
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
            BigDecimal rate = params.get("rate");
            if (rate == null) {
                throw new IllegalArgumentException("Missing required parameter 'rate' for FIXED strategy"); //TODO handle this more gracefully
            }
            return new FixedRule(rate);
        }
    }
}
