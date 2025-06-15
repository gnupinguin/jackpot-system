package io.gnupinguin.sporty.interview.persistence.model;

import jakarta.annotation.Nonnull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("bet")
public record Bet(
        @Id Long id,
        long userId,
        long jackpotId,
        boolean processed,
        BigDecimal amount,
        Instant createdAt) {

    @Nonnull
    public Bet process() {
        return new Bet(id, userId, jackpotId, true, amount, createdAt);
    }
}
