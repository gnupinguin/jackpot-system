package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ExtendedJackpotRepositoryImpl implements ExtendedJackpotRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<RewardedBet> findRewardedBetByBetId(long betId) {
        var sql = """
                    SELECT b.id as bet_id,
                        b.processed as bet_processed,
                        b.jackpot_id as jackpot_id, r.id as reward_id,
                        r.reward_amount as reward_amount, r.created_at as rewarded_at
                    FROM "bet" b
                    LEFT JOIN "jackpot_reward" r ON b.id = r.bet_id
                    WHERE b.id = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RewardedBet(
                rs.getLong("bet_id"),
                rs.getBoolean("bet_processed"),
                rs.getLong("jackpot_id"),
                rs.getLong("reward_id"),
                rs.getBigDecimal("reward_amount"),
                getRewardedAt(rs)
        ), betId).stream().findFirst();
    }

    private static Instant getRewardedAt(ResultSet rs) throws SQLException {
        var rewardedAt = rs.getTimestamp("rewarded_at");
        return rewardedAt == null ? null : rewardedAt.toInstant();
    }

}
