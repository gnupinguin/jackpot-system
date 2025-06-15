package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRuleParam;
import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface JackpotRuleParamRepository extends CrudRepository<JackpotRuleParam, Long> {

    List<JackpotRuleParam> findAllByRuleId(@Nonnull Long ruleId);

    default Map<String, BigDecimal> findParamsByRuleId(@Nonnull Long ruleId) {
        var map = findAllByRuleId(ruleId).stream()
                .collect(Collectors.toMap(JackpotRuleParam::paramName, JackpotRuleParam::paramValue));
        if (map.isEmpty()) {
            throw new IllegalArgumentException("No parameters found for rule ID: " + ruleId);
        }
        return map;
    }

}
