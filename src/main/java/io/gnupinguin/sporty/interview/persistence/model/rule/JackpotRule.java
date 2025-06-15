package io.gnupinguin.sporty.interview.persistence.model.rule;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table(name = "jackpot_rule")
public record JackpotRule(
        @Id Long id,
        JackpotRuleType type,
        RuleStrategy strategy,
        String name,
        Instant createdAt
) {}
