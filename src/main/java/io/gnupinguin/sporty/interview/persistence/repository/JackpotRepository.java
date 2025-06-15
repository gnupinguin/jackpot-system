package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import jakarta.annotation.Nonnull;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface JackpotRepository extends CrudRepository<Jackpot, Long> {
    final String INCREMENT_JACKPOT_POOL_QUERY = "UPDATE \"jackpot\" SET current_pool_amount = current_pool_amount + ?, updated_at = ? WHERE id = ?";
    ;
}
