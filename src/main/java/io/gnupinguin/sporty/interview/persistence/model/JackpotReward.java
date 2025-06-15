package io.gnupinguin.sporty.interview.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("jackpot_reward")
public record JackpotReward(
        @Id Long id,
        Long betId,
        Long userId,
        Long jackpotId,
        BigDecimal rewardAmount,
        Instant createdAt
) {}
