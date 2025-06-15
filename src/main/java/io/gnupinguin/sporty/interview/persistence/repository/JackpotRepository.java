package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JackpotRepository extends CrudRepository<Jackpot, Long> {
    String INCREMENT_JACKPOT_POOL_QUERY = "UPDATE \"jackpot\" SET current_pool_amount = current_pool_amount + ?, updated_at = ? WHERE id = ?";
    String RESET_JACKPOT_POOL_QUERY = "UPDATE \"jackpot\" SET current_pool_amount = initial_pool_amount, updated_at = ? WHERE id = ?";
    String SELECT_FOR_UPDATE_QUERY = "SELECT * FROM \"jackpot\" WHERE id = ? FOR UPDATE";
}
