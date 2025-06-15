package io.gnupinguin.sporty.interview.async.processor;

import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRule;
import io.gnupinguin.sporty.interview.persistence.model.rule.JackpotRuleParam;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleParamRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractJackpotRuleContributor implements JackpotRuleContributor {

    private final JackpotRuleParamRepository ruleParamRepository;

    @Nonnull
    protected <T> T loadRule(@Nonnull JackpotRule rule, @Nonnull Function<Map<String, BigDecimal>, T> mapper) {
        var params = ruleParamRepository.findAllByRuleId(rule.id()).stream()
                .collect(Collectors.toMap(JackpotRuleParam::paramName, JackpotRuleParam::paramValue));
        if (params.isEmpty()) {
            throw new IllegalStateException("No parameters found for rule: " + rule.id());
        }
        return mapper.apply(params);
    }

    @Nonnull
    protected static BigDecimal requireParam(Map<String, BigDecimal> params, String key) {
        var value = params.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter '" + key + "' for ?? strategy"); //TODO handle this more gracefully
        }
        return value;
    }

}
