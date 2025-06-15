-- Alice
INSERT INTO "user" (name) VALUES ('Alice'); -- suppose generated id = 1

-- Bob
INSERT INTO "user" (name) VALUES ('Bob'); -- suppose generated id = 2


-- Contribution Rule 1: FIXED
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('CONTRIBUTION', 'FIXED', 'Fixed Contribution Rule', NOW()); -- id = 1

-- Contribution Rule 2: VARIABLE
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('CONTRIBUTION', 'VARIABLE', 'Variable Contribution Rule', NOW()); -- id = 2

-- Reward Rule
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('REWARD', 'FIXED', 'Reward Rule', NOW()); -- id = 3

-- For Contribution Rule 1 (id = 1) fixed
INSERT INTO "jackpot_rule_param" (rule_id, param_name, param_value)
VALUES (1, 'rate', 0.05); -- 5% contribution

-- For Contribution Rule 2 (id = 2) variable
INSERT INTO "jackpot_rule_param" (rule_id, param_name, param_value)
VALUES
  (2, 'initial_rate', 0.10),     -- 10%
  (2, 'min_rate', 0.02),         -- 2%
  (2, 'decrease_step', 1000),    -- every 1000 in pool
  (2, 'decrease_rate', 0.005);   -- decrease by 0.5%

-- Starter Jackpot with Contribution Rule 1 and Reward Rule 3
INSERT INTO "jackpot" (
    name, initial_pool_amount, current_pool_amount,
    contribution_rule_id, reward_rule_id,
    created_at, updated_at
) VALUES (
    'Starter Jackpot', 1000.00, 1000.00,
    1, 3, NOW(), NOW()
); -- id = 1

-- Mega Jackpot with Contribution Rule 2 and Reward Rule 3
INSERT INTO "jackpot" (
    name, initial_pool_amount, current_pool_amount,
    contribution_rule_id, reward_rule_id,
    created_at, updated_at
) VALUES (
    'Mega Jackpot', 5000.00, 5000.00,
    2, 3, NOW(), NOW()
); -- id = 2
