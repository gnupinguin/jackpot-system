package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;

import java.util.Optional;

public interface ExtendedJackpotRepository {

    Optional<RewardedBet> findRewardedBetByBetId(long betId);

}
