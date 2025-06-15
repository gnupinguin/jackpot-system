package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;
import jakarta.annotation.Nonnull;

import java.util.Optional;

public interface ExtendedJackpotRepository {

    Optional<Jackpot> findForUpdate(long id);

    @Nonnull
    Jackpot update(@Nonnull Jackpot jackpot);

    Optional<RewardedBet> findRewardedBetByBetId(long betId);

}
