# Aesthetica - Database Schema Reference

## Complete Database Schema

This document provides a detailed reference of all database tables, columns, and relationships in the Aesthetica e-commerce platform.

---

## Table of Contents

1. [Core Tables](#core-tables)
2. [Relationship Tables](#relationship-tables)
3. [Reference Tables](#reference-tables)
4. [Column Definitions](#column-definitions)
5. [Indexes & Constraints](#indexes--constraints)
6. [Named Queries](#named-queries)
7. [Data Access Patterns](#data-access-patterns)

---

## Core Tables

### users

Stores customer/buyer account information.

```sql
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    mobile VARCHAR(10) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status_id INT NOT NULL,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,
    FOREIGN KEY (status_id) REFERENCES status(id),
    INDEX idx_email (email),
    INDEX idx_mobile (mobile),
    INDEX idx_status_id (status_id)
);
```

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique user identifier |
| first_name | VARCHAR(255) | NOT NULL | User's first name |
| last_name | VARCHAR(255) | NOT NULL | User's last name |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Email for login/contact (indexed) |
| password | VARCHAR(255) | NOT NULL | Hashed password |
| mobile | VARCHAR(10) | NOT NULL, UNIQUE | Phone number (Sri Lankan format) |
| status_id | INT | NOT NULL, FK | Links to status table |
| created_at | DATETIME(0) | NOT NULL | Account creation timestamp |
| updated_at | DATETIME(0) | NOT NULL | Last update timestamp |

**Sample Data**:
```sql
INSERT INTO users (first_name, last_name, email, password, mobile, status_id, created_at, updated_at)
VALUES ('John', 'Doe', 'john@example.com', 'hashed_password', '0712345678', 1, NOW(), NOW());
```

**Relationships**:
- Foreign Key: `status_id` → `status.id`
- Referenced by: `address.user_id`, `cart.user_id`, `orders.users_id`

---

### product

Stores product/item listings.

```sql
CREATE TABLE product (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    weight DOUBLE NOT NULL DEFAULT 1.0,
    length DOUBLE NOT NULL DEFAULT 1.0,
    width DOUBLE NOT NULL DEFAULT 1.0,
    height DOUBLE NOT NULL DEFAULT 1.0,
    seller_id INT,
    category_id INT,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES seller(id),
    FOREIGN KEY (category_id) REFERENCES category(id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_category_id (category_id),
    FULLTEXT INDEX ftx_title_desc (title, description)
);
```

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique product identifier |
| title | VARCHAR(200) | NOT NULL | Product name/title |
| description | TEXT | NOT NULL | Detailed product description |
| weight | DOUBLE | NOT NULL, DEFAULT 1.0 | Product weight in kg |
| length | DOUBLE | NOT NULL, DEFAULT 1.0 | Shipping dimension in cm |
| width | DOUBLE | NOT NULL, DEFAULT 1.0 | Shipping dimension in cm |
| height | DOUBLE | NOT NULL, DEFAULT 1.0 | Shipping dimension in cm |
| seller_id | INT | FK (NULL OK) | Seller who lists product |
| category_id | INT | FK | Product category |
| created_at | DATETIME(0) | NOT NULL | Creation timestamp |
| updated_at | DATETIME(0) | NOT NULL | Update timestamp |

**Notes**:
- Seller is optional (NULL allowed) - enables platform-managed products
- Dimensions default to 1.0 for safe calculations
- FULLTEXT index enables natural language search on title/description

---

### stock

Represents price variants/batches of products (inventory management).

```sql
CREATE TABLE stock (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    manufactured_date DATE,
    expiry_date DATE,
    discount_id INT,
    status_id INT NOT NULL,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (discount_id) REFERENCES discount(id),
    FOREIGN KEY (status_id) REFERENCES status(id),
    INDEX idx_product_id (product_id),
    INDEX idx_status_id (status_id),
    INDEX idx_expiry_date (expiry_date)
);
```

**Purpose**: A single product can have multiple stock records with:
- Different prices over time
- Different quantities in different batches
- Different expiry dates (for perishable goods)
- Different discounts applied

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique stock identifier |
| product_id | INT | NOT NULL, FK | Links to product |
| price | DOUBLE | NOT NULL | Selling price for this batch |
| quantity | INT | NOT NULL | Available units |
| manufactured_date | DATE | | Production date |
| expiry_date | DATE | | Expiration date (for perishables) |
| discount_id | INT | FK | Discount applied to this stock |
| status_id | INT | NOT NULL, FK | ACTIVE, INACTIVE, DISCONTINUED |
| created_at | DATETIME(0) | NOT NULL | Creation timestamp |
| updated_at | DATETIME(0) NOT NULL | Last updated timestamp |

**Example Scenario**:
```
Product: Apple Juice 1L
├─ Stock 1: price=150 LKR, qty=500, mfg=2026-01-01, exp=2026-04-01
├─ Stock 2: price=140 LKR, qty=300, mfg=2026-02-01, exp=2026-05-01
└─ Stock 3: price=160 LKR, qty=100, mfg=2026-03-01, exp=2026-06-01
```

---

### cart

Shopping cart items for users.

```sql
CREATE TABLE cart (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    stock_id INT NOT NULL,
    qty INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (stock_id) REFERENCES stock(id),
    INDEX idx_user_id (user_id),
    INDEX idx_stock_id (stock_id)
);
```

**Purpose**: Temporary storage of items before checkout.

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique cart item ID |
| user_id | INT | NOT NULL, FK | Logged-in user |
| stock_id | INT | NOT NULL, FK | Product variant selected |
| qty | INT | NOT NULL | Quantity user wants |

**Lifecycle**:
1. User adds product → Cart record created
2. User updates quantity → qty updated
3. User removes → Cart record deleted
4. User checkout → Cart items converted to OrderItems, cart cleared

---

### orders

Completed/pending customer orders.

```sql
CREATE TABLE orders (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    users_id INT NOT NULL,
    delivery_types_id INT NOT NULL,
    status_id INT NOT NULL,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,
    FOREIGN KEY (users_id) REFERENCES users(id),
    FOREIGN KEY (delivery_types_id) REFERENCES delivery_types(id),
    FOREIGN KEY (status_id) REFERENCES status(id),
    INDEX idx_user_id (users_id),
    INDEX idx_status_id (status_id),
    INDEX idx_created_at (created_at)
);
```

**Purpose**: Header record for orders; actual items stored in order_items.

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique order ID |
| users_id | INT | NOT NULL, FK | Customer who placed order |
| delivery_types_id | INT | NOT NULL, FK | Delivery method selected |
| status_id | INT | NOT NULL, FK | PENDING, PACKING, DELIVERED, COMPLETED |
| created_at | DATETIME(0) | NOT NULL | Order placement time |
| updated_at | DATETIME(0) | NOT NULL | Last status update |

**Status Flow**: PENDING → PACKING → DELIVERED → COMPLETED

---

### order_items

Individual items within an order (line items).

```sql
CREATE TABLE order_items (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    orders_id INT NOT NULL,
    stocks_id INT NOT NULL,
    seller_id INT,
    quantity INT NOT NULL,
    rating INT,
    FOREIGN KEY (orders_id) REFERENCES orders(id),
    FOREIGN KEY (stocks_id) REFERENCES stock(id),
    FOREIGN KEY (seller_id) REFERENCES seller(id),
    INDEX idx_order_id (orders_id),
    INDEX idx_seller_id (seller_id)
);
```

**Purpose**: Stores details of each product in an order; allows per-item seller tracking and ratings.

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique line item ID |
| orders_id | INT | NOT NULL, FK | Parent order |
| stocks_id | INT | NOT NULL, FK | Which product variant |
| seller_id | INT | FK | Seller who provided this item |
| quantity | INT | NOT NULL | Units ordered of this item |
| rating | INT | | Customer rating (0-5) after delivery |

**Example Order**:
```
Order #123
├─ OrderItem 1: Product=Laptop, Stock=5000/unit, Qty=1, Seller=TechShop
├─ OrderItem 2: Product=Mouse, Stock=2500/unit, Qty=2, Seller=TechShop
└─ OrderItem 3: Product=Keyboard, Stock=4000/unit, Qty=1, Seller=ElectroStore
```

---

## Relationship Tables

### address

User delivery/billing addresses.

```sql
CREATE TABLE address (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    line_one VARCHAR(45) NOT NULL,
    line_two VARCHAR(45),
    postal_code VARCHAR(10),
    city_id INT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (city_id) REFERENCES city(id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_primary (is_primary)
);
```

**Purpose**: Stores multiple addresses per user; one marked as primary.

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique address ID |
| user_id | INT | NOT NULL, FK | User who owns address |
| line_one | VARCHAR(45) | NOT NULL | Street address |
| line_two | VARCHAR(45) | | Additional address info |
| postal_code | VARCHAR(10) | | Zip/postal code |
| city_id | INT | FK | City reference |
| is_primary | BOOLEAN | NOT NULL, DEFAULT FALSE | Default address for delivery |

**Usage**:
1. During registration/profile setup: User adds addresses
2. During checkout: User selects address or adds new one
3. Address.getPrimary named query retrieves default

---

### seller

Marketplace seller profiles.

```sql
CREATE TABLE seller (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    address_id INT,
    company_name VARCHAR(200) NOT NULL,
    company_mobile VARCHAR(10) NOT NULL,
    company_email VARCHAR(100) NOT NULL,
    status_id INT,
    created_at DATETIME(0) NOT NULL,
    updated_at DATETIME(0) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (address_id) REFERENCES address(id),
    FOREIGN KEY (status_id) REFERENCES status(id),
    INDEX idx_company_email (company_email)
);
```

**Purpose**: Extended profile for sellers in the marketplace.

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique seller ID |
| user_id | INT | FK | Associated user account |
| company_name | VARCHAR(200) | NOT NULL | Business name |
| company_mobile | VARCHAR(10) | NOT NULL | Business phone |
| company_email | VARCHAR(100) | NOT NULL | Business email |
| address_id | INT | FK | Business address |
| status_id | INT | FK | PENDING, APPROVED, REJECTED, ACTIVE |
| created_at | DATETIME(0) | NOT NULL | Seller registration date |
| updated_at | DATETIME(0) | NOT NULL | Last update |

**Status Workflow**:
1. Seller registers → PENDING
2. Admin approval → APPROVED (can list products)
3. Admin rejection → REJECTED

---

## Reference Tables

### status

Enumeration of status values used throughout system.

```sql
CREATE TABLE status (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(45) NOT NULL UNIQUE
);
```

**Predefined Values**:

| id | value | Used In |
|----|-------|---------|
| 1 | ACTIVE | Users, Products, Sellers (normal state) |
| 2 | INACTIVE | Users (account disabled) |
| 3 | BLOCKED | Users (violators) |
| 4 | DEACTIVATE | Users (soft delete) |
| 5 | VERIFIED | Users (email verified) |
| 6 | PENDING | Orders (awaiting processing), Sellers (awaiting approval) |
| 7 | PACKING | Orders (being prepared) |
| 8 | DELIVERED | Orders (in transit/delivered) |
| 9 | COMPLETED | Orders (finished) |
| 10 | CANCELLED | Orders (user cancelled) |
| 11 | APPROVED | Sellers (cleared to sell) |
| 12 | REJECTED | Sellers (not approved) |

**Design Pattern**: Master status table prevents inconsistencies; foreign keys enforce referential integrity.

---

### category

Product categories/classifications.

```sql
CREATE TABLE category (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    icon VARCHAR(255)
);
```

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique category ID |
| name | VARCHAR(255) | NOT NULL, UNIQUE | Category name |
| icon | VARCHAR(255) | | Path to category icon |

**Sample Categories**:
```
Electronics, Clothing, Books, Home & Garden, Beauty, Sports
```

---

### city

Delivery cities/regions for address and delivery calculations.

```sql
CREATE TABLE city (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique city ID |
| name | VARCHAR(50) | NOT NULL, UNIQUE | City name |

**Sample Data** (Sri Lankan cities):
```
Colombo, Kandy, Galle, Jaffna, Kurunegala, Matara, Trincomalee
```

---

### delivery_types

Shipping/delivery options available.

```sql
CREATE TABLE delivery_types (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price DOUBLE NOT NULL
);
```

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique delivery type ID |
| name | VARCHAR(50) | NOT NULL, UNIQUE | Delivery method |
| price | DOUBLE | NOT NULL | Shipping cost |

**Standard Options**:

| name | price | Description |
|------|-------|-------------|
| WITHIN_CITY | 300.00 | Local city delivery (2-3 days) |
| OUT_OF_CITY | 500.00 | Regional delivery (5-7 days) |

---

### discount

Discount codes and coupon management.

```sql
CREATE TABLE discount (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    coupon_code VARCHAR(45) NOT NULL UNIQUE,
    value DOUBLE NOT NULL,
    started_at DATETIME NOT NULL,
    expiered_at DATETIME NOT NULL
);
```

**Columns**:

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| id | INT | PK, AUTO_INCREMENT | Unique discount ID |
| coupon_code | VARCHAR(45) | NOT NULL, UNIQUE | Code entered by user |
| value | DOUBLE | NOT NULL | Discount amount (LKR) or percentage |
| started_at | DATETIME | NOT NULL | When coupon becomes valid |
| expiered_at | DATETIME | NOT NULL | When coupon expires |

**Default Coupon**:
```sql
coupon_code: 'DEFAULT'
value: 0.00
started_at: NOW()
expiered_at: NOW() + 10 YEARS
-- Always available, no discount
```

---

### product_images

Element collection table for product images.

```sql
CREATE TABLE product_images (
    pr_id INT NOT NULL,
    images VARCHAR(255),
    PRIMARY KEY (pr_id),
    FOREIGN KEY (pr_id) REFERENCES product(id)
);
```

**Purpose**: Stores multiple image URLs per product (created automatically by Hibernate @ElementCollection).

---

## Column Definitions

### Data Types Reference

| Type | MySQL | Java | Purpose |
|------|-------|------|---------|
| INT | 4 bytes | int/Integer | Whole numbers |
| VARCHAR(n) | Variable | String | Text up to n chars |
| TEXT | 65K | String | Large text fields |
| DOUBLE | 8 bytes | Double | Decimal numbers |
| BOOLEAN | TINYINT(1) | boolean | True/false |
| DATE | 3 bytes | LocalDate | Dates only |
| DATETIME(0) | 8 bytes | LocalDateTime | Date + time, no microseconds |

### Nullable Columns

Some columns allow NULL to represent optional data:

| Column | Table | Reason |
|--------|-------|--------|
| line_two | address | Additional address line optional |
| postal_code | address | May not be required in all regions |
| city_id | address | For flexible address formats |
| seller_id | product | Product doesn't require seller (platform admin products) |
| seller_id | order_items | For backward compatibility |
| discount_id | stock | Stock may not have discount |
| expiery_date | stock | Only for perishable goods |
| ratings | order_items | Initial NULL, filled after delivery |

---

## Indexes & Constraints

### Performance Indexes

```sql
-- Email lookup (login)
CREATE INDEX idx_email ON users(email);

-- Orders by user (my account page)
CREATE INDEX idx_user_id ON orders(users_id);

-- Orders by status (admin dashboard)
CREATE INDEX idx_status_id ON orders(status_id);

-- Recent orders
CREATE INDEX idx_created_at ON orders(created_at DESC);

-- Product search
CREATE FULLTEXT INDEX ftx_title_desc ON product(title, description);
```

### Unique Constraints

```sql
-- Prevent duplicate emails
UNIQUE KEY (email) ON users

-- One address per user as primary (business rule in app)
-- No DB constraint; enforced by application

-- Unique status values
UNIQUE KEY (value) ON status

-- Unique coupon codes
UNIQUE KEY (coupon_code) ON discount
```

### Foreign Key Constraints

All foreign keys use default behavior:
- **ON DELETE**: RESTRICT (prevent deletion if referenced)
- **ON UPDATE**: RESTRICT (prevent updates if referenced)

```sql
-- Example
ALTER TABLE cart ADD CONSTRAINT fk_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT;
```

---

## Named Queries

Hibernate Named Queries pre-compile common queries for optimization and consistency.

### Query: User.getByEmail

```sql
SELECT user FROM User user WHERE user.email = :email
```

**Purpose**: Find user by email during login/registration check

**Usage**:
```java
User user = session.createNamedQuery("User.getByEmail", User.class)
    .setParameter("email", "user@example.com")
    .getSingleResultOrNull();
```

---

### Query: Status.findByValue

```sql
SELECT status FROM Status status WHERE status.value = :value
```

**Purpose**: Get status entity by string value

**Usage**:
```java
Status active = session.createNamedQuery("Status.findByValue", Status.class)
    .setParameter("value", "ACTIVE")
    .getSingleResultOrNull();
```

---

### Query: Address.getPrimary

```sql
SELECT address FROM Address address 
WHERE address.user.id = :user 
ORDER BY address.id ASC 
LIMIT 1
```

**Purpose**: Retrieve user's primary address for checkout

**Usage**:
```java
Address primary = session.createNamedQuery("Address.getPrimary", Address.class)
    .setParameter("user", userId)
    .getSingleResultOrNull();
```

---

### Query: Discount.findDefault

```sql
SELECT discount FROM Discount discount 
WHERE discount.couponCode = 'DEFAULT'
```

**Purpose**: Get default discount (0%) for orders without coupon

---

### Query: DeliveryType.findByName

```sql
SELECT dt FROM DeliveryType dt 
WHERE dt.name = :name
```

**Purpose**: Look up delivery cost by type

---

## Data Access Patterns

### Create User

```sql
INSERT INTO users (first_name, last_name, email, password, mobile, status_id, created_at, updated_at)
VALUES (?, ?, ?, ?, ?, 1, NOW(), NOW());
```

### Add to Cart

```sql
INSERT INTO cart (user_id, stock_id, qty)
VALUES (?, ?, ?);
```

### Checkout Flow

```sql
-- 1. Create order header
INSERT INTO orders (users_id, delivery_types_id, status_id, created_at, updated_at)
VALUES (?, ?, 
    (SELECT id FROM status WHERE value = 'PENDING'),
    NOW(), NOW()
);

-- 2. Create line items from cart
INSERT INTO order_items (orders_id, stocks_id, seller_id, quantity, rating)
SELECT 
    LAST_INSERT_ID(),
    c.stock_id,
    s.seller_id,
    c.qty,
    0
FROM cart c
JOIN stock s ON c.stock_id = s.id
WHERE c.user_id = ?;

-- 3. Update stock quantities
UPDATE stock s
SET s.quantity = s.quantity - (
    SELECT SUM(oi.quantity)
    FROM order_items oi
    WHERE oi.stocks_id = s.id AND oi.orders_id = LAST_INSERT_ID()
)
WHERE s.id IN (
    SELECT DISTINCT stock_id FROM cart WHERE user_id = ?
);

-- 4. Clear cart
DELETE FROM cart WHERE user_id = ?;
```

### Get Order with Items

```sql
SELECT 
    o.id, o.status_id, o.created_at,
    dt.name as delivery_type, dt.price,
    oi.id, oi.stocks_id, oi.quantity, oi.rating,
    p.id, p.title, p.description,
    st.price as item_price,
    sel.company_name
FROM orders o
LEFT JOIN delivery_types dt ON o.delivery_types_id = dt.id
LEFT JOIN order_items oi ON o.id = oi.orders_id
LEFT JOIN stock st ON oi.stocks_id = st.id
LEFT JOIN product p ON st.product_id = p.id
LEFT JOIN seller sel ON oi.seller_id = sel.id
WHERE o.users_id = ? AND o.id = ?;
```

---

## Evolution & Migrations

### Migration History

**2026-02-20**: Latest migration
- Made seller_id optional in product and order_items
- Set default dimensions for products (1.0)
- Ensured required statuses exist
- Removed old email verification columns

Future migrations would follow SQL scripts in `db_migration_YYYY_MM_DD.sql` files.

---

## Conclusion

The Aesthetica database schema is normalized to 3NF (Third Normal Form), balancing:
- **Normalization**: Avoiding data redundancy
- **Query Performance**: Strategic indexes and denormalization where needed
- **Scalability**: Foreign keys support referential integrity
- **Flexibility**: NULL columns and optional relationships allow feature evolution

The design supports multi-seller marketplace operations with clear separation of user roles (customers, sellers, admins) and complete order/product lifecycle management.

