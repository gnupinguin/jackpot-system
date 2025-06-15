CREATE TABLE jackpot_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    strategy VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    created_at TIMESTAMP
);

CREATE TABLE jackpot_rule_param (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL REFERENCES jackpot_rule(id),
    "key" VARCHAR(100) NOT NULL,
    "value" DECIMAL(18, 4) NOT NULL
);

CREATE TABLE jackpot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,

    initial_pool_amount DECIMAL(18, 2) NOT NULL,
    current_pool_amount DECIMAL(18, 2) NOT NULL,

    contribution_rule_id BIGINT NOT NULL REFERENCES jackpot_rule(id),
    reward_rule_id BIGINT NOT NULL REFERENCES jackpot_rule(id),

    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE bet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    jackpot_id BIGINT NOT NULL REFERENCES jackpot(id),
    amount DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE jackpot_contribution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bet_id BIGINT NOT NULL REFERENCES bet(id),
    user_id BIGINT NOT NULL,
    jackpot_id BIGINT NOT NULL REFERENCES jackpot(id),

    stake_amount DECIMAL(18, 2) NOT NULL,
    contribution_amount DECIMAL(18, 2) NOT NULL,
    jackpot_pool_after DECIMAL(18, 2) NOT NULL,

    created_at TIMESTAMP
);

CREATE TABLE jackpot_reward (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bet_id BIGINT NOT NULL REFERENCES bet(id),
    user_id BIGINT NOT NULL,
    jackpot_id BIGINT NOT NULL REFERENCES jackpot(id),

    reward_amount DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE "user" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
);
