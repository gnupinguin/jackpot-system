package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.JackpotContribution;
import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;
import jakarta.annotation.Nonnull;

import java.util.Optional;

public interface ExtendedJackpotRepository {

    Optional<Jackpot> findForUpdate(long id);

    @Nonnull
    Jackpot update(@Nonnull Jackpot jackpot);

    boolean incrementPool(@Nonnull JackpotContribution contribution, long version);

    boolean resetJackpot(@Nonnull JackpotReward reward, long currentVersion);

    Optional<RewardedBet> findRewardedBetByBetId(long betId);

}
