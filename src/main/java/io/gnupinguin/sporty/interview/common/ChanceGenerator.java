package io.gnupinguin.sporty.interview.common;

import jakarta.annotation.Nonnull;

import java.math.BigDecimal;

public interface ChanceGenerator {

    boolean won(@Nonnull BigDecimal chance);

}
