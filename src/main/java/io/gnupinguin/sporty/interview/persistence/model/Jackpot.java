package io.gnupinguin.sporty.interview.persistence.model;

import jakarta.annotation.Nonnull;
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
        Instant updatedAt) {

    public Jackpot resetPool(@Nonnull Instant rewardTime) {
        return new Jackpot(
                id,
                name,
                initialPoolAmount,
                initialPoolAmount,
                contributionRuleId,
                rewardRuleId,
                createdAt,
                rewardTime);
    }

    public Jackpot updatePool(@Nonnull BigDecimal newPoolAmount, @Nonnull Instant updatedAt) {
        return new Jackpot(
                id,
                name,
                initialPoolAmount,
                newPoolAmount,
                contributionRuleId,
                rewardRuleId,
                createdAt,
                updatedAt);
    }

}
