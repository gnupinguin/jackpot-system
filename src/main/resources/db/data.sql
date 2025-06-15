-- Users
INSERT INTO "user" (name) VALUES ('Alice'); -- id = 1
INSERT INTO "user" (name) VALUES ('Bob');   -- id = 2

-- Contribution Rule 1: FIXED (id = 1)
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('CONTRIBUTION', 'FIXED', 'Fixed Contribution Rule', CURRENT_TIMESTAMP);

INSERT INTO "jackpot_rule_param" (rule_id, param_name, param_value)
VALUES (1, 'rate', 0.05);

-- Contribution Rule 2: VARIABLE (id = 2)
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('CONTRIBUTION', 'VARIABLE', 'Variable Contribution Rule', CURRENT_TIMESTAMP);

INSERT INTO "jackpot_rule_param" (rule_id, param_name, param_value)
VALUES
  (2, 'initial_rate', 0.10),
  (2, 'min_rate', 0.02),
  (2, 'decrease_step', 1000),
  (2, 'decrease_rate', 0.005);

-- Reward Rule 3: FIXED 90% Chance
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('REWARD', 'FIXED', 'Fixed 90% Reward Chance', CURRENT_TIMESTAMP); -- id = 3

INSERT INTO "jackpot_rule_param" (rule_id, param_name, param_value)
VALUES (3, 'chance', 0.90);

-- Reward Rule 4: VARIABLE Reward Chance (scaling)
INSERT INTO "jackpot_rule" (type, strategy, name, created_at)
VALUES ('REWARD', 'VARIABLE', 'Variable Reward Chance (scaling)', CURRENT_TIMESTAMP); -- id = 4

INSERT INTO "jackpot_rule_param" (rule_id, param_name, param_value)
VALUES
  (4, 'max_chance', 1.0),
  (4, 'increase_rate', 0.0005),
  (4, 'trigger_pool', 20000.00);

-- Jackpot 1: Starter Jackpot (contribution rule 1, reward rule 3)
INSERT INTO "jackpot" (
    name, initial_pool_amount, current_pool_amount,
    contribution_rule_id, reward_rule_id,
    created_at, updated_at
) VALUES (
    'Starter Jackpot', 1000.00, 1000.00,
    1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Jackpot 2: Mega Jackpot (contribution rule 2, reward rule 4)
INSERT INTO "jackpot" (
    name, initial_pool_amount, current_pool_amount,
    contribution_rule_id, reward_rule_id,
    created_at, updated_at
) VALUES (
    'Mega Jackpot', 5000.00, 5000.00,
    2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
