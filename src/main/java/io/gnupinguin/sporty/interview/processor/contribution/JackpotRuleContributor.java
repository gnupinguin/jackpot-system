package io.gnupinguin.sporty.interview.processor.contribution;

import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import jakarta.annotation.Nonnull;

public interface JackpotRuleContributor {

    @Nonnull
    RuleStrategy getStrategy();

    @Nonnull
    JackpotContribution contribute(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet);

}
