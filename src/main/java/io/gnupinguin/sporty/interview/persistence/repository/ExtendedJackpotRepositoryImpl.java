package io.gnupinguin.sporty.interview.persistence.repository;

import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.model.RewardedBet;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ExtendedJackpotRepositoryImpl implements ExtendedJackpotRepository {

    private final JackpotRepository repository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Optional<Jackpot> findForUpdate(long id) {
        return Optional.ofNullable(findByIdForUpdate(id));
    }

    @Nonnull
    @Override
    @Transactional
    public Jackpot update(@Nonnull Jackpot jackpot) {
        return repository.save(jackpot);
    }

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

    @Nullable
    private Jackpot findByIdForUpdate(long id) {
        String sql = """
                        SELECT id, name, initial_pool_amount, current_pool_amount,
                           contribution_rule_id, reward_rule_id, created_at, updated_at
                        FROM "jackpot"
                        WHERE id = ?
                        FOR UPDATE
                      """;

        return jdbcTemplate.queryForObject(sql, jackpotRowMapper(), id);
    }

    private RowMapper<Jackpot> jackpotRowMapper() {
        return (rs, rowNum) -> new Jackpot(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getBigDecimal("initial_pool_amount"),
                rs.getBigDecimal("current_pool_amount"),
                rs.getLong("contribution_rule_id"),
                rs.getLong("reward_rule_id"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

}
