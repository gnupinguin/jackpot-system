package io.gnupinguin.sporty.interview.persistence.model.rule;

import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("jackpot_rule_param")
public record JackpotRuleParam(
        Long id,
        Long ruleId,
        String paramName,
        BigDecimal paramValue
) {}
