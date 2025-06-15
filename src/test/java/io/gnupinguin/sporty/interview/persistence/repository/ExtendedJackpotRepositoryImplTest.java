package io.gnupinguin.sporty.interview.persistence.repository;


import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtendedJackpotRepositoryImplTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ExtendedJackpotRepositoryImpl repository;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    private Jackpot jackpot;
    private final Instant now = Instant.parse("2025-06-13T12:00:00Z");

    @BeforeEach
    void setUp() {
        jackpot = new Jackpot(
                1L,
                "Test Jackpot",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1200),
                10L,
                20L,
                now,
                now
        );
    }

    @Test
    void testReturnJackpotWhenFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1L)))
                .thenReturn(jackpot);

        Optional<Jackpot> result = repository.findForUpdate(1L);

        assertTrue(result.isPresent());
        assertEquals(jackpot, result.get());

        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), any(RowMapper.class), eq(1L));
        String actualSql = sqlCaptor.getValue();
        assertEquals(minimizeQuery("""
                        SELECT id, name, initial_pool_amount, current_pool_amount,
                           contribution_rule_id, reward_rule_id, created_at, updated_at
                        FROM "jackpot"
                        WHERE id = ?
                        FOR UPDATE
                      """), minimizeQuery(actualSql));
    }

    @Test
    void testFindForUpdateReturnEmptyWhenNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(99L)))
                .thenReturn(null);

        Optional<Jackpot> result = repository.findForUpdate(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void update_shouldDelegateToJpaRepository() {
        when(jackpotRepository.save(jackpot)).thenReturn(jackpot);

        Jackpot result = repository.update(jackpot);

        assertNotNull(result);
        assertEquals(jackpot, result);
        verify(jackpotRepository).save(jackpot);
    }

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