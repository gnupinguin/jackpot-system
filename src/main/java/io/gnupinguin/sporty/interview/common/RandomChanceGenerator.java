package io.gnupinguin.sporty.interview.common;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Random;

@RequiredArgsConstructor
public class RandomChanceGenerator implements ChanceGenerator {

    private final Random random;

    @Override
    public boolean won(@Nonnull BigDecimal chance) {
        return random.nextDouble() <= chance.doubleValue();
    }

}
