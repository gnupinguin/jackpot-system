package io.gnupinguin.sporty.interview.async.processor.reward;

import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.RuleStrategy;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface JackpotRuleRewarder {

    @Nonnull
    RuleStrategy getStrategy();

    @Nullable
    JackpotReward reward(@Nonnull Jackpot jackpot, @Nonnull JackpotRule rule, @Nonnull Bet bet);

}
