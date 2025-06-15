package io.gnupinguin.sporty.interview.processor;

import jakarta.annotation.Nonnull;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Map;

@UtilityClass
public class JackpotRuleProcessorHelper {

    @Nonnull
    public static BigDecimal requireParam(Map<String, BigDecimal> params, String key) {
        var value = params.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter '" + key + "' for ?? strategy"); //TODO handle this more gracefully
        }
        return value;
    }
}
