package io.gnupinguin.sporty.interview.rest;
import io.gnupinguin.sporty.interview.async.BetPublisher;
import io.gnupinguin.sporty.interview.async.processor.reward.JackpotRuleRewarderProvider;
import io.gnupinguin.sporty.interview.persistence.model.Bet;
import io.gnupinguin.sporty.interview.persistence.model.Jackpot;
import io.gnupinguin.sporty.interview.persistence.repository.BetRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRewardRepository;
import io.gnupinguin.sporty.interview.persistence.repository.JackpotRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;


@Slf4j
@RestController
@RequestMapping("bet")
@RequiredArgsConstructor
public class BetEndpoint {

    private final Clock clock;
    private final BetRepository betRepository;
    private final JackpotRepository jackpotRepository;
    private final JackpotRuleRepository ruleRepository;
    private final JackpotRewardRepository rewardRepository;
    private final BetPublisher publisher;
    private final JackpotRuleRewarderProvider rewarderProvider;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("place")
    @Transactional
    public Bet place(@RequestBody BetResource request) {
        //TODO: validate request
        log.info("Place bet request: {}", request);
        try {
            var bet = betRepository.save(new Bet(null, request.userId(), request.jackpotId(), request.amount(), clock.instant()));
            publisher.publishAsync(bet);
            return bet;
        } catch (Exception e) {
            log.error("Error placing bet", e);
            throw new RuntimeException("Failed to place bet", e); //TODO global error handling
        }
    }

    @PostMapping("check-reward")
    @Transactional
    public RewardResource check(@RequestParam("betId") Long betId) {
        log.info("Check bet with ID: {}", betId);
        var bet = betRepository.findById(betId)
                .orElseThrow(() -> new RuntimeException("Bet not found with ID: " + betId)); //TODO global error handling
        var jackpot = findByIdForUpdate(bet.jackpotId());
        var jackpotRule = ruleRepository.findById(jackpot.rewardRuleId())
                .orElseThrow(() -> new RuntimeException("Jackpot rule not found with ID: " + jackpot.contributionRuleId())); //TODO global error handling
        var rewarder = rewarderProvider.getRewarder(jackpotRule.strategy());
        try {
            var reward = rewarder.reward(jackpot, jackpotRule, bet);
            log.info("Rewarded bet with ID: {}. Reward: {}", betId, reward);
            if (reward != null) {
                rewardRepository.save(reward);
                resetJackpotPool(jackpot.id());
                return new RewardResource(true, reward.rewardAmount());
            } else {
                log.info("No reward for bet with ID: {}", betId);
            }
        } catch (Exception e) {
            log.error("Error checking bet with ID: {}", betId, e);
            throw new RuntimeException("Failed to check bet", e); //TODO global error handling
        }
        return new RewardResource(false, null);
    }

    private Jackpot findByIdForUpdate(Long id) {
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

    private void resetJackpotPool(long jackpotId) {
        jdbcTemplate.update(JackpotRepository.RESET_JACKPOT_POOL_QUERY, clock.instant(), jackpotId);
        log.info("Reset jackpot pool for ID: {}", jackpotId);
    }

}
