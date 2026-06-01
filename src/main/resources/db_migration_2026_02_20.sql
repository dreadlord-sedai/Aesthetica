-- Aesthetica DB migration (2026-02-20)
-- Purpose:
-- 1) Remove seller-account requirement for adding products
-- 2) Normalize status usage (ACTIVE default for users/products)
-- 3) Remove old verification columns (if present)
-- 4) Stop depending on package dimensions from UI (keep DB columns with safe defaults)

USE `aesthetica`;

-- -----------------------------
-- Ensure required statuses exist
-- -----------------------------
INSERT INTO `status` (`value`)
SELECT 'ACTIVE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'ACTIVE');

INSERT INTO `status` (`value`)
SELECT 'PENDING' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'PENDING');

INSERT INTO `status` (`value`)
SELECT 'COMPLETED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'COMPLETED');

INSERT INTO `status` (`value`)
SELECT 'REJECTED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'REJECTED');

INSERT INTO `status` (`value`)
SELECT 'APPROVED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'APPROVED');

-- ---------------------------------
-- Set ACTIVE as default user status
-- ---------------------------------
SET @active_status_id := (SELECT `id` FROM `status` WHERE `value` = 'ACTIVE' LIMIT 1);

UPDATE `users`
SET `status_id` = @active_status_id
WHERE `status_id` IS NULL;

-- ------------------------------------
-- Remove email-verification old columns
-- ------------------------------------
SET @has_user_verification_col := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'verification'
);
SET @sql_drop_user_verification := IF(
    @has_user_verification_col > 0,
    'ALTER TABLE `user` DROP COLUMN `verification`',
    'SELECT ''user.verification does not exist'''
);
PREPARE stmt_drop_user_verification FROM @sql_drop_user_verification;
EXECUTE stmt_drop_user_verification;
DEALLOCATE PREPARE stmt_drop_user_verification;

SET @has_user_verification_code_col := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'verification_code'
);
SET @sql_drop_user_verification_code := IF(
    @has_user_verification_code_col > 0,
    'ALTER TABLE `user` DROP COLUMN `verification_code`',
    'SELECT ''user.verification_code does not exist'''
);
PREPARE stmt_drop_user_verification_code FROM @sql_drop_user_verification_code;
EXECUTE stmt_drop_user_verification_code;
DEALLOCATE PREPARE stmt_drop_user_verification_code;

-- ------------------------------------------------
-- Product dimensions: keep schema but enforce safe defaults
-- (UI no longer sends dimensions for product adding)
-- ------------------------------------------------
UPDATE `product`
SET
    `weight` = CASE WHEN `weight` IS NULL OR `weight` <= 0 THEN 1.0 ELSE `weight` END,
    `length` = CASE WHEN `length` IS NULL OR `length` <= 0 THEN 1.0 ELSE `length` END,
    `width`  = CASE WHEN `width`  IS NULL OR `width`  <= 0 THEN 1.0 ELSE `width`  END,
    `height` = CASE WHEN `height` IS NULL OR `height` <= 0 THEN 1.0 ELSE `height` END;

ALTER TABLE `product`
    MODIFY COLUMN `weight` DOUBLE NOT NULL DEFAULT 1.0,
    MODIFY COLUMN `length` DOUBLE NOT NULL DEFAULT 1.0,
    MODIFY COLUMN `width`  DOUBLE NOT NULL DEFAULT 1.0,
    MODIFY COLUMN `height` DOUBLE NOT NULL DEFAULT 1.0;

-- ----------------------------------------------------
-- Seller not required for selling new products anymore
-- Keep seller table for historical compatibility
-- ----------------------------------------------------
ALTER TABLE `product`
    MODIFY COLUMN `seller_id` INT NULL;

ALTER TABLE `order_items`
    MODIFY COLUMN `seller_id` INT NULL;

-- Optional cleanup (uncomment only if you want to clear seller profiles):
-- DELETE FROM `seller`;

-- Done
SELECT 'Aesthetica migration 2026-02-20 completed' AS migration_status;
