package io.gnupinguin.sporty.interview.persistence.model.rule;

import java.math.BigDecimal;
import java.util.UUID;

public record JackpotRuleParam(
        Long id,
        UUID ruleId,
        String key,
        BigDecimal value
) {}
