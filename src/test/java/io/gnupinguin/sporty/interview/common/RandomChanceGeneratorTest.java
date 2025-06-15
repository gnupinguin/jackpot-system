package io.gnupinguin.sporty.interview.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomChanceGeneratorTest {

    @Mock
    private Random random;

    @InjectMocks
    private RandomChanceGenerator chanceGenerator;

    @Test
    void won_returnsTrue_whenRandomIsLessThanOrEqualToChance() {
        when(random.nextDouble()).thenReturn(0.5);

        assertTrue(chanceGenerator.won(BigDecimal.valueOf(0.6)));
        assertTrue(chanceGenerator.won(BigDecimal.valueOf(0.5)));
    }

    @Test
    void won_returnsFalse_whenRandomIsGreaterThanChance() {
        when(random.nextDouble()).thenReturn(0.7);

        assertFalse(chanceGenerator.won(BigDecimal.valueOf(0.6)));
    }

    @Test
    void won_handlesEdgeCases() {
        when(random.nextDouble()).thenReturn(0.0);
        assertTrue(chanceGenerator.won(BigDecimal.ZERO));

        when(random.nextDouble()).thenReturn(1.0);
        assertFalse(chanceGenerator.won(BigDecimal.valueOf(0.99)));
    }
}