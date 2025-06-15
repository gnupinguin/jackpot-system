package io.gnupinguin.sporty.interview.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("jackpot")
public record Jackpot(
        @Id Long id,
        String name,
        BigDecimal initialPoolAmount,
        BigDecimal currentPoolAmount,
        long contributionRuleId,
        long rewardRuleId,
        Instant createdAt,
        Instant updatedAt
) {}
