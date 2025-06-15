package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.JackpotReward;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JackpotRewardRepository extends CrudRepository<JackpotReward, Long> {

    Optional<JackpotReward> findByBetId(long betId);

}
