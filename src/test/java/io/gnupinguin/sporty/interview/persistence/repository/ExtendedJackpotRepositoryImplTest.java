package io.gnupinguin.sporty.interview.persistence.repository;


import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtendedJackpotRepositoryImplTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ExtendedJackpotRepositoryImpl repository;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    private final Instant now = Instant.parse("2025-06-13T12:00:00Z");

    @Test
    void findRewardedBetByBetId_shouldReturnRewardedBet_whenExists() {
        RewardedBet expected = new RewardedBet(
                123L,
                true,
                1L,
                999L,
                new BigDecimal("300.00"),
                now
        );

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(123L)))
                .thenReturn(List.of(expected));

        Optional<RewardedBet> result = repository.findRewardedBetByBetId(123L);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());

        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), eq(123L));
        String sql = sqlCaptor.getValue();
        assertEquals(minimizeQuery("""
                SELECT b.id as bet_id,
                       b.processed as bet_processed,
                       b.jackpot_id as jackpot_id, r.id as reward_id,
                       r.reward_amount as reward_amount, r.created_at as rewarded_at
                FROM "bet" b
                LEFT JOIN "jackpot_reward" r ON b.id = r.bet_id
                WHERE b.id = ?
                """), minimizeQuery(sql));
    }

    @Test
    void findRewardedBetByBetId_shouldReturnEmpty_whenNoReward() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(456L)))
                .thenReturn(List.of());

        Optional<RewardedBet> result = repository.findRewardedBetByBetId(456L);

        assertTrue(result.isEmpty());

        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq(456L));
    }

    private String minimizeQuery(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

}