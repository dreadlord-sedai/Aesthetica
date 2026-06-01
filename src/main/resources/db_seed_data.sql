-- Aesthetica seed data
-- Safe to run multiple times (uses NOT EXISTS checks)

USE `aesthetica`;

-- ---------------------------------
-- 1) Core status values
-- ---------------------------------
INSERT INTO `status` (`value`)
SELECT 'ACTIVE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'ACTIVE');

INSERT INTO `status` (`value`)
SELECT 'DEACTIVATE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'DEACTIVATE');

INSERT INTO `status` (`value`)
SELECT 'PENDING' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'PENDING');

INSERT INTO `status` (`value`)
SELECT 'INACTIVE' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'INACTIVE');

INSERT INTO `status` (`value`)
SELECT 'BLOCKED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'BLOCKED');

INSERT INTO `status` (`value`)
SELECT 'DELIVERED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'DELIVERED');

INSERT INTO `status` (`value`)
SELECT 'PACKING' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'PACKING');

INSERT INTO `status` (`value`)
SELECT 'APPROVED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'APPROVED');

INSERT INTO `status` (`value`)
SELECT 'REJECTED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'REJECTED');

INSERT INTO `status` (`value`)
SELECT 'CANCELLED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'CANCELLED');

INSERT INTO `status` (`value`)
SELECT 'VERIFIED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'VERIFIED');

INSERT INTO `status` (`value`)
SELECT 'RECEIVED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'RECEIVED');

INSERT INTO `status` (`value`)
SELECT 'COMPLETED' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `status` WHERE `value` = 'COMPLETED');

-- ---------------------------------
-- 2) Delivery types required by checkout
-- ---------------------------------
INSERT INTO `delivery_types` (`name`, `price`)
SELECT 'WITHIN_CITY', 300.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `delivery_types` WHERE `name` = 'WITHIN_CITY');

INSERT INTO `delivery_types` (`name`, `price`)
SELECT 'OUT_OF_CITY', 500.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `delivery_types` WHERE `name` = 'OUT_OF_CITY');

-- ---------------------------------
-- 3) Default discount row
-- ---------------------------------
INSERT INTO `discount` (`coupon_code`, `value`, `started_at`, `expiered_at`)
SELECT 'DEFAULT', 0.00, NOW(), DATE_ADD(NOW(), INTERVAL 10 YEAR) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `discount` WHERE `coupon_code` = 'DEFAULT');

-- ---------------------------------
-- 4) Master data: cities and categories
-- ---------------------------------
INSERT INTO `city` (`name`)
SELECT 'Colombo' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `name` = 'Colombo');

INSERT INTO `city` (`name`)
SELECT 'Kandy' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `name` = 'Kandy');

INSERT INTO `city` (`name`)
SELECT 'Galle' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `name` = 'Galle');

INSERT INTO `city` (`name`)
SELECT 'Jaffna' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `name` = 'Jaffna');

INSERT INTO `city` (`name`)
SELECT 'Kurunegala' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `name` = 'Kurunegala');

INSERT INTO `category` (`name`, `icon`)
SELECT 'Electronics', 'assets/images/category/electronics.png' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = 'Electronics');

INSERT INTO `category` (`name`, `icon`)
SELECT 'Fashion', 'assets/images/category/fashion.png' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = 'Fashion');

INSERT INTO `category` (`name`, `icon`)
SELECT 'Home & Living', 'assets/images/category/home.png' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = 'Home & Living');

INSERT INTO `category` (`name`, `icon`)
SELECT 'Beauty', 'assets/images/category/beauty.png' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `category` WHERE `name` = 'Beauty');

-- ---------------------------------
-- 5) Users (plain password, same as app login logic)
-- ---------------------------------
SET @active_status_id := (SELECT `id` FROM `status` WHERE `value` = 'ACTIVE' LIMIT 1);

INSERT INTO `users` (`first_name`, `last_name`, `mobile`, `email`, `password`, `status_id`, `created_at`, `updated_at`)
SELECT 'Admin', 'User', '0771234567', 'admin@aesthetica.com', 'Admin@123', @active_status_id, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `users` WHERE `email` = 'admin@aesthetica.com');

INSERT INTO `users` (`first_name`, `last_name`, `mobile`, `email`, `password`, `status_id`, `created_at`, `updated_at`)
SELECT 'Nimal', 'Perera', '0711234567', 'nimal@aesthetica.com', 'Nimal@123', @active_status_id, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `users` WHERE `email` = 'nimal@aesthetica.com');

INSERT INTO `users` (`first_name`, `last_name`, `mobile`, `email`, `password`, `status_id`, `created_at`, `updated_at`)
SELECT 'Kasuni', 'Silva', '0729876543', 'kasuni@aesthetica.com', 'Kasuni@123', @active_status_id, NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `users` WHERE `email` = 'kasuni@aesthetica.com');

-- ---------------------------------
-- 6) Addresses for sample users
-- ---------------------------------
SET @colombo_city_id := (SELECT `id` FROM `city` WHERE `name` = 'Colombo' LIMIT 1);
SET @kandy_city_id := (SELECT `id` FROM `city` WHERE `name` = 'Kandy' LIMIT 1);

SET @admin_user_id := (SELECT `id` FROM `users` WHERE `email` = 'admin@aesthetica.com' LIMIT 1);
SET @nimal_user_id := (SELECT `id` FROM `users` WHERE `email` = 'nimal@aesthetica.com' LIMIT 1);

INSERT INTO `address` (`line_one`, `line_two`, `postal_code`, `city_id`, `user_id`, `is_primary`)
SELECT 'No. 10, Galle Road', 'Kollupitiya', '00300', @colombo_city_id, @admin_user_id, 1 FROM DUAL
WHERE @admin_user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `address` WHERE `user_id` = @admin_user_id);

INSERT INTO `address` (`line_one`, `line_two`, `postal_code`, `city_id`, `user_id`, `is_primary`)
SELECT 'No. 22, Peradeniya Road', 'Kandy Central', '20000', @kandy_city_id, @nimal_user_id, 1 FROM DUAL
WHERE @nimal_user_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `address` WHERE `user_id` = @nimal_user_id);

-- ---------------------------------
-- 7) Sample products (seller_id kept NULL by current app flow)
-- ---------------------------------
SET @electronics_cat_id := (SELECT `id` FROM `category` WHERE `name` = 'Electronics' LIMIT 1);
SET @fashion_cat_id := (SELECT `id` FROM `category` WHERE `name` = 'Fashion' LIMIT 1);
SET @beauty_cat_id := (SELECT `id` FROM `category` WHERE `name` = 'Beauty' LIMIT 1);

INSERT INTO `product` (`title`, `description`, `weight`, `length`, `width`, `height`, `seller_id`, `category_id`, `created_at`, `updated_at`)
SELECT 'Aesthetica Wireless Headset', 'Lightweight wireless headset with clean dark aesthetic design.', 1.00, 1.00, 1.00, 1.00, NULL, @electronics_cat_id, NOW(), NOW() FROM DUAL
WHERE @electronics_cat_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `title` = 'Aesthetica Wireless Headset');

INSERT INTO `product` (`title`, `description`, `weight`, `length`, `width`, `height`, `seller_id`, `category_id`, `created_at`, `updated_at`)
SELECT 'Urban Black Hoodie', 'Premium cotton hoodie with minimal print and relaxed fit.', 1.00, 1.00, 1.00, 1.00, NULL, @fashion_cat_id, NOW(), NOW() FROM DUAL
WHERE @fashion_cat_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `title` = 'Urban Black Hoodie');

INSERT INTO `product` (`title`, `description`, `weight`, `length`, `width`, `height`, `seller_id`, `category_id`, `created_at`, `updated_at`)
SELECT 'Midnight Glow Serum', 'Night skin serum with hyaluronic formula for smooth finish.', 1.00, 1.00, 1.00, 1.00, NULL, @beauty_cat_id, NOW(), NOW() FROM DUAL
WHERE @beauty_cat_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `title` = 'Midnight Glow Serum');

-- ---------------------------------
-- 8) Stocks for sample products
-- ---------------------------------
SET @stock_status_id := (SELECT `id` FROM `status` WHERE `value` = 'ACTIVE' LIMIT 1);
SET @default_discount_id := (SELECT `id` FROM `discount` WHERE `coupon_code` = 'DEFAULT' LIMIT 1);

SET @p_headset_id := (SELECT `id` FROM `product` WHERE `title` = 'Aesthetica Wireless Headset' LIMIT 1);
SET @p_hoodie_id := (SELECT `id` FROM `product` WHERE `title` = 'Urban Black Hoodie' LIMIT 1);
SET @p_serum_id := (SELECT `id` FROM `product` WHERE `title` = 'Midnight Glow Serum' LIMIT 1);

INSERT INTO `stock` (`product_id`, `price`, `quantity`, `manufactured_date`, `expiry_date`, `discount_id`, `status_id`, `created_at`, `updated_at`)
SELECT @p_headset_id, 18990.00, 50, NULL, NULL, @default_discount_id, @stock_status_id, NOW(), NOW() FROM DUAL
WHERE @p_headset_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `stock` WHERE `product_id` = @p_headset_id);

INSERT INTO `stock` (`product_id`, `price`, `quantity`, `manufactured_date`, `expiry_date`, `discount_id`, `status_id`, `created_at`, `updated_at`)
SELECT @p_hoodie_id, 7990.00, 80, NULL, NULL, @default_discount_id, @stock_status_id, NOW(), NOW() FROM DUAL
WHERE @p_hoodie_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `stock` WHERE `product_id` = @p_hoodie_id);

INSERT INTO `stock` (`product_id`, `price`, `quantity`, `manufactured_date`, `expiry_date`, `discount_id`, `status_id`, `created_at`, `updated_at`)
SELECT @p_serum_id, 4590.00, 120, NULL, NULL, @default_discount_id, @stock_status_id, NOW(), NOW() FROM DUAL
WHERE @p_serum_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `stock` WHERE `product_id` = @p_serum_id);

-- ---------------------------------
-- 9) Product image links
-- ---------------------------------
INSERT INTO `product_images` (`pr_id`, `images`)
SELECT @p_headset_id, 'assets/images/productimages/headset-1.jpg' FROM DUAL
WHERE @p_headset_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `product_images`
      WHERE `pr_id` = @p_headset_id AND `images` = 'assets/images/productimages/headset-1.jpg'
  );

INSERT INTO `product_images` (`pr_id`, `images`)
SELECT @p_hoodie_id, 'assets/images/productimages/hoodie-1.jpg' FROM DUAL
WHERE @p_hoodie_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `product_images`
      WHERE `pr_id` = @p_hoodie_id AND `images` = 'assets/images/productimages/hoodie-1.jpg'
  );

INSERT INTO `product_images` (`pr_id`, `images`)
SELECT @p_serum_id, 'assets/images/productimages/serum-1.jpg' FROM DUAL
WHERE @p_serum_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `product_images`
      WHERE `pr_id` = @p_serum_id AND `images` = 'assets/images/productimages/serum-1.jpg'
  );

-- ---------------------------------
-- 10) Optional cart row for quick checkout testing
-- ---------------------------------
SET @nimal_stock_id := (SELECT s.`id` FROM `stock` s WHERE s.`product_id` = @p_headset_id LIMIT 1);

INSERT INTO `cart` (`qty`, `user_id`, `stock_id`)
SELECT 1, @nimal_user_id, @nimal_stock_id FROM DUAL
WHERE @nimal_user_id IS NOT NULL
  AND @nimal_stock_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `cart` WHERE `user_id` = @nimal_user_id AND `stock_id` = @nimal_stock_id
  );

SELECT 'Aesthetica seed script completed' AS seed_status;
