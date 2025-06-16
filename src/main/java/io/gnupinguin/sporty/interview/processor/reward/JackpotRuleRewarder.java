package io.gnupinguin.sporty.interview.processor.reward;

import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface JackpotRuleRewarder {

    @Nonnull
    RuleStrategy getStrategy();

    @Nullable
    JackpotReward reward(@Nonnull JackpotRule rule, @Nonnull JackpotContribution contribution);

}
