CREATE TABLE "user" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE "jackpot_rule" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    strategy VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE "jackpot_rule_param" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL REFERENCES "jackpot_rule"(id),
    param_name VARCHAR(100) NOT NULL,
    param_value DECIMAL(18, 4) NOT NULL
);

CREATE UNIQUE INDEX uq_rule_id_param_name ON "jackpot_rule_param" (rule_id, param_name);

CREATE TABLE "jackpot" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,

    initial_pool_amount DECIMAL(18, 2) NOT NULL,
    current_pool_amount DECIMAL(18, 2) NOT NULL,

    contribution_rule_id BIGINT NOT NULL REFERENCES "jackpot_rule"(id),
    reward_rule_id BIGINT NOT NULL REFERENCES "jackpot_rule"(id),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE "bet" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    jackpot_id BIGINT NOT NULL REFERENCES "jackpot"(id),
    amount DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE "jackpot_contribution" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bet_id BIGINT NOT NULL REFERENCES "bet"(id),
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    jackpot_id BIGINT NOT NULL REFERENCES "jackpot"(id),

    stake_amount DECIMAL(18, 2) NOT NULL,
    contribution_amount DECIMAL(18, 2) NOT NULL,
    jackpot_pool_after DECIMAL(18, 2) NOT NULL,

    created_at TIMESTAMP NOT NULL
);

CREATE TABLE "jackpot_reward" (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bet_id BIGINT NOT NULL REFERENCES "bet"(id),
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    jackpot_id BIGINT NOT NULL REFERENCES "jackpot"(id),

    reward_amount DECIMAL(18, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
ALTER TABLE "jackpot_reward" ADD CONSTRAINT uq_jackpot_reward UNIQUE (jackpot_id);

