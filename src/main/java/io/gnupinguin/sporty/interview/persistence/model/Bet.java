package io.gnupinguin.sporty.interview.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("bet")
public record Bet(
        @Id Long id,
        Long userId,
        Long jackpotId,
        BigDecimal amount,
        Instant createdAt
) {}
