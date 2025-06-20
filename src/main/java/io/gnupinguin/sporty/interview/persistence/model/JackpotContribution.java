package io.gnupinguin.sporty.interview.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("jackpot_contribution")
public record JackpotContribution(
        @Id Long id,
        Long betId,
        Long userId,
        Long jackpotId,
        BigDecimal stakeAmount,
        BigDecimal contributionAmount,
        BigDecimal jackpotPoolAfter,
        Instant createdAt
) {}
