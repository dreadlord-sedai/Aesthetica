# Aesthetica - Complete Project Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [File Structure](#file-structure)
5. [Database & Hibernate](#database--hibernate)
6. [Entity Relationships](#entity-relationships)
7. [Core Components](#core-components)
8. [API Endpoints](#api-endpoints)
9. [Data Flow](#data-flow)
10. [Setup & Deployment](#setup--deployment)
11. [File-by-File Explanation](#file-by-file-explanation)

---

## Project Overview

**Aesthetica** is a full-featured e-commerce web application built using modern Java technologies. It provides a comprehensive marketplace platform where users can browse products, manage shopping carts, place orders, and handle payments through PayHere integration.

### Key Features:
- User authentication and account management
- Product catalog with categories and inventory management
- Shopping cart functionality
- Order management and checkout
- Multi-seller support
- Delivery options with pricing
- Discount and coupon system
- Email notifications
- Responsive web interface

### Target Users:
- Customers (Buyers)
- Sellers
- Administrators

---

## Architecture

### Architectural Pattern: **Layered/N-Tier Architecture**

The application follows a clean separation of concerns with distinct layers:

```
┌─────────────────────────────────┐
│   Presentation Layer (JSP/HTML) │
├─────────────────────────────────┤
│   REST API Layer (Jersey)       │
├─────────────────────────────────┤
│   Middleware & Filters          │
├─────────────────────────────────┤
│   Business Logic Layer          │
│   (Services)                    │
├─────────────────────────────────┤
│   Data Access Layer             │
│   (Hibernate ORM)               │
├─────────────────────────────────┤
│   Database Layer (MySQL)        │
└─────────────────────────────────┘
```

### Key Architectural Components:

1. **Embedded Tomcat Server**: The application uses embedded Tomcat (version 10.1.7) for hosting, allowing standalone deployment without external server configuration.

2. **Jersey REST Framework**: Provides RESTful API endpoints for client-server communication.

3. **Hibernate ORM**: Maps Java objects to database tables, handling all database operations transparently.

4. **Session Management**: HTTP sessions manage user authentication state across requests.

5. **Middleware/Filters**: Authentication and authorization filters protect resources.

---

## Technology Stack

### Backend Technologies:

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 17 |
| **Build Tool** | Maven | 3.x |
| **Web Server** | Apache Tomcat (Embedded) | 10.1.7 |
| **REST Framework** | Jersey | 3.1.2 |
| **ORM** | Hibernate | 6.1.7.Final |
| **Database Driver** | MySQL Connector | 9.0.0 |
| **Validator** | Hibernate Validator | 8.0.0.Final |

### Frontend Technologies:

| Component | Technology |
|-----------|-----------|
| **Markup** | HTML5 |
| **Styling** | CSS3 (Bootstrap + Custom) |
| **Scripting** | JavaScript (ES6+) |
| **UI Library** | Bootstrap |
| **Notifications** | Notiflix |
| **Slider** | noUiSlider |

### Additional Libraries:

| Library | Purpose | Version |
|---------|---------|---------|
| **GSON** | JSON serialization/deserialization | 2.10.1 |
| **Jakarta Mail** | Email functionality | 2.0.1+ |
| **Rocketbase Email Template** | Email template builder | 2.4.0 |
| **Commons IO** | File handling utilities | 2.16.1 |
| **Jakarta Servlet API** | Servlet implementation | 6.1.0 |

### Database:
- **MySQL** 5.7+ (Primary)
- **H2** (In-memory fallback)

---

## File Structure

```
Aesthetica/
│
├── pom.xml                          # Maven configuration & dependencies
├── Aesthetica.iml                   # IntelliJ project file
├── nb-configuration.xml             # NetBeans configuration
│
├── src/
│   ├── main/
│   │   ├── java/com/aesthetica/
│   │   │   ├── Main.java                    # Application entry point
│   │   │   │
│   │   │   ├── Annotation/                  # Custom annotations
│   │   │   │   └── IsUser.java              # User authentication annotation
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java           # Jersey configuration
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── TestController.java      # Testing endpoint
│   │   │   │   └── api/                     # REST API Controllers
│   │   │   │       ├── UserController.java
│   │   │   │       ├── ProductController.java
│   │   │   │       ├── CartController.java
│   │   │   │       ├── CheckoutController.java
│   │   │   │       ├── ProfileController.java
│   │   │   │       └── ContentController.java
│   │   │   │
│   │   │   ├── dto/                         # Data Transfer Objects
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── CartDTO.java
│   │   │   │   ├── CheckoutDTO.java
│   │   │   │   ├── CheckoutRequestDTO.java
│   │   │   │   ├── StockDTO.java
│   │   │   │   ├── CityDTO.java
│   │   │   │   ├── DeliveryTypeDTO.java
│   │   │   │   ├── PayHereDTO.java
│   │   │   │   └── UserAddressDTO.java
│   │   │   │
│   │   │   ├── entity/                      # JPA Entities (ORM-mapped)
│   │   │   │   ├── BaseEntity.java          # Base class with timestamps
│   │   │   │   ├── User.java                # User entity
│   │   │   │   ├── Product.java             # Product entity
│   │   │   │   ├── Stock.java               # Product stock variant
│   │   │   │   ├── Category.java            # Product categories
│   │   │   │   ├── Cart.java                # Shopping cart items
│   │   │   │   ├── Order.java               # Customer orders
│   │   │   │   ├── OrderItem.java           # Items in an order
│   │   │   │   ├── Address.java             # User addresses
│   │   │   │   ├── City.java                # Cities for delivery
│   │   │   │   ├── Seller.java              # Seller profiles
│   │   │   │   ├── Status.java              # Status reference table
│   │   │   │   ├── DeliveryType.java        # Delivery options
│   │   │   │   ├── Discount.java            # Discount codes
│   │   │   │   └── PreparationState.java    # Order preparation states
│   │   │   │
│   │   │   ├── listener/
│   │   │   │   └── ContextPathListener.java # Application lifecycle listener
│   │   │   │
│   │   │   ├── mail/                        # Email functionality
│   │   │   │   └── [Mail service classes]
│   │   │   │
│   │   │   ├── middleware/
│   │   │   │   ├── AuthAccessFilter.java    # Sign-up/Sign-in filter
│   │   │   │   ├── AccessControlFilter.java # Protected pages filter
│   │   │   │   └── AuthFilter.java          # API authentication filter
│   │   │   │
│   │   │   ├── provider/                    # JAX-RS providers
│   │   │   │   └── [Exception mappers, etc]
│   │   │   │
│   │   │   ├── service/                     # Business logic
│   │   │   │   ├── UserService.java         # User operations
│   │   │   │   ├── ProductService.java      # Product operations
│   │   │   │   ├── CartService.java         # Cart operations
│   │   │   │   ├── OrderService.java        # Order operations
│   │   │   │   ├── CheckoutService.java     # Checkout logic
│   │   │   │   ├── ProfileService.java      # User profile
│   │   │   │   ├── CityService.java         # City data
│   │   │   │   └── ContentService.java      # Content/static data
│   │   │   │
│   │   │   ├── util/                        # Utility classes
│   │   │   │   ├── HibernateUtil.java       # Hibernate SessionFactory
│   │   │   │   ├── AppUtil.java             # Application utilities
│   │   │   │   ├── PayHereUtil.java         # Payment integration
│   │   │   │   └── Env.java                 # Environment configuration
│   │   │   │
│   │   │   └── validation/
│   │   │       └── Validator.java           # Input validation rules
│   │   │
│   │   ├── resources/
│   │   │   ├── app.properties               # Application configuration
│   │   │   ├── hibernate.cfg.xml            # Hibernate configuration
│   │   │   ├── db_migration_2026_02_20.sql # Database migrations
│   │   │   └── db_seed_data.sql             # Initial database data
│   │   │
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml                  # Servlet configuration
│   │       │   ├── classes/                 # Compiled resources
│   │       │   └── lib/                     # Runtime libraries
│   │       ├── index.html                   # Home page
│   │       ├── shop.html                    # Product listing
│   │       ├── single-product-view.html     # Product detail
│   │       ├── all_categories.html          # Categories page
│   │       ├── checkout.html                # Checkout page
│   │       ├── my_account.html              # User profile
│   │       ├── contact.html                 # Contact page
│   │       ├── sign_in.html                 # Login page
│   │       ├── sign_up.html                 # Registration page
│   │       └── assets/
│   │           ├── css/                     # Stylesheets
│   │           ├── js/                      # JavaScript files
│   │           ├── images/                  # Image assets
│   │           ├── icons/                   # Icon assets
│   │           ├── fonts/                   # Custom fonts
│   │           └── bootstrap/               # Bootstrap framework
│   │
│   └── test/
│       └── java/
│           └── Test.java                    # Unit tests
│
├── scripts/
│   └── populate_db.sh                       # Database initialization script
│
└── target/
    ├── aesthetica.war                       # Compiled WAR file
    ├── classes/                             # Compiled Java classes
    └── [Build artifacts]
```

---

## Database & Hibernate

### Database Configuration

**Location**: `src/main/resources/hibernate.cfg.xml`

```xml
Connection Settings:
- Driver: com.mysql.cj.jdbc.Driver
- Dialect: org.hibernate.dialect.MySQLDialect
- URL: jdbc:mysql://localhost:3306/aesthetica
- Username: aesthetica_user
- Password: mysql2006
- hbm2ddl.auto: update (auto-update schema)
```

### Fallback Configuration

If MySQL is unavailable, the application automatically falls back to an in-memory H2 database to maintain functionality during development/testing.

### Hibernate Configuration Features:

1. **Auto Schema Generation**: `hibernate.hbm2ddl.auto=update` automatically creates/updates tables based on entity annotations.
2. **SQL Logging**: `hibernate.show_sql=true` logs all SQL queries for debugging.
3. **Named Queries**: Pre-compiled queries for common operations improve performance.
4. **Lazy Loading**: Relationships use LAZY fetching by default to avoid N+1 query problems.

### HibernateUtil Class

**Purpose**: Manages the SessionFactory lifecycle using lazy initialization pattern.

**Key Features**:
- Singleton pattern with thread-safe initialization
- Lazy initialization prevents errors if DB is unavailable at startup
- Automatic fallback to H2 if MySQL fails
- Provides static access to SessionFactory throughout application

```java
SessionFactory sf = HibernateUtil.getSessionFactory();
Session session = sf.openSession();
```

---

## Entity Relationships

### Entity Diagram Overview

```
User (1) ────→ (M) Address
  │ (1)
  ├─────→ (M) Cart
  │
  └─────→ (M) Order
           │
           └──→ (M) OrderItem ─→ (1) Stock ─→ (1) Product ─→ (1) Seller
                     │
                     └──→ (1) Seller


Product (1) ────→ (M) Stock
   │ (1)
   ├─────→ (M) Category
   │         (1)
   │
   └─────→ (1) Seller


Cart (M) ⟵── (1) Stock
  │
  ├─ User (M)
  └─ Stock (1) ─→ Product ─→ Seller


Order (1) ────→ (1) Status
  │ (1)
  ├─────→ (M) OrderItem
  │         │
  │         ├─ (1) Stock
  │         ├─ (1) Seller
  │         └─ (1) Order
  │
  └─────→ (1) DeliveryType


Seller (1) ────→ (1) User
  │  (1)
  ├─────→ (1) Address
  │
  └─────→ (M) Product
```

### Entity Descriptions

#### 1. **User Entity**
- **Table**: `users`
- **Purpose**: Represents a customer/buyer in the system
- **Key Fields**:
  - `id` (PK): Auto-generated user ID
  - `firstName`, `lastName`: User's name
  - `email`: Unique email (indexed for login)
  - `password`: Hashed password
  - `mobile`: Phone number (10 digits)
  - `status_id` (FK): References Status entity (ACTIVE, INACTIVE, BLOCKED, etc.)
  - `created_at`, `updated_at`: Timestamps inherited from BaseEntity
- **Relationships**:
  - OneToMany with Address (User has multiple addresses)
  - OneToMany with Cart (User has cart items)
  - OneToMany with Order (User has multiple orders)

#### 2. **Product Entity**
- **Table**: `product`
- **Purpose**: Represents a product/item available for sale
- **Key Fields**:
  - `id` (PK): Auto-generated product ID
  - `title`: Product name (max 200 chars)
  - `description`: Product details (TEXT)
  - `weight`, `length`, `width`, `height`: Shipping dimensions (defaults: 1.0)
  - `seller_id` (FK): Optional reference to Seller (NULL allowed)
  - `category_id` (FK): References Category
- **Special Features**:
  - `images`: ElementCollection storing image paths from `product_images` table
- **Relationships**:
  - ManyToOne with Seller (optional, multiple products per seller)
  - OneToMany with Stock (product has multiple price/quantity variants)
  - ManyToOne with Category
  - Inherits `created_at`, `updated_at` from BaseEntity

#### 3. **Stock Entity**
- **Table**: `stock`
- **Purpose**: Represents a price variant/batch of a product
- **Key Fields**:
  - `id` (PK): Auto-generated stock ID
  - `product_id` (FK): References Product
  - `price`: Selling price
  - `quantity`: Available items
  - `manufactured_date`: Production date
  - `expiry_date`: Expiration date (for perishables)
  - `discount_id` (FK): References Discount
  - `status_id` (FK): References Status
- **Purpose**: Allows same product to have multiple prices/batches with different expiry dates

#### 4. **Cart Entity**
- **Table**: `cart`
- **Purpose**: Represents items in a user's shopping cart
- **Key Fields**:
  - `id` (PK): Auto-generated cart item ID
  - `user_id` (FK): References User
  - `stock_id` (FK): References Stock (the specific price variant)
  - `qty`: Quantity in cart
- **Relationships**:
  - ManyToOne with User
  - ManyToOne with Stock

#### 5. **Order Entity**
- **Table**: `orders`
- **Purpose**: Represents a completed/pending order
- **Key Fields**:
  - `id` (PK): Auto-generated order ID
  - `user_id` (FK): References User (customer)
  - `delivery_types_id` (FK): References DeliveryType
  - `status_id` (FK): References Status (PENDING, PACKING, DELIVERED, etc.)
  - `created_at`, `updated_at`: Order timestamps
- **Relationships**:
  - ManyToOne with User
  - ManyToOne with DeliveryType
  - ManyToOne with Status
  - OneToMany with OrderItem (order contains multiple items)

#### 6. **OrderItem Entity**
- **Table**: `order_items`
- **Purpose**: Represents individual items within an order
- **Key Fields**:
  - `id` (PK): Auto-generated order item ID
  - `order_id` (FK): References Order
  - `stocks_id` (FK): References Stock
  - `seller_id` (FK): References Seller (who provides this item)
  - `quantity`: How many units ordered
  - `rating`: Customer rating (0-5)
- **Purpose**: Allows tracking of each item's seller and rating separately

#### 7. **Address Entity**
- **Table**: `address`
- **Purpose**: Stores delivery/billing addresses for users
- **Key Fields**:
  - `id` (PK): Auto-generated address ID
  - `user_id` (FK): References User
  - `line_one`: Street address line 1
  - `line_two`: Street address line 2 (optional)
  - `postalCode`: Zip/postal code
  - `city_id` (FK): References City
  - `is_primary`: Boolean marking default address
- **Named Query**: `Address.getPrimary` retrieves user's primary address

#### 8. **Category Entity**
- **Table**: `category`
- **Purpose**: Product categories/classifications
- **Key Fields**:
  - `id` (PK): Auto-generated category ID
  - `name`: Category name
  - `icon`: Path to category icon
- **Relationships**:
  - OneToMany with Product (category contains multiple products)

#### 9. **Seller Entity**
- **Table**: `seller`
- **Purpose**: Represents a seller/vendor in the marketplace
- **Key Fields**:
  - `id` (PK): Auto-generated seller ID
  - `company_name`: Business name
  - `company_mobile`: Business phone
  - `company_email`: Business email
  - `user_id` (FK): OneToOne with User (seller is also a user)
  - `address_id` (FK): OneToOne with Address (business address)
  - `status_id` (FK): References Status (ACTIVE, INACTIVE, APPROVED, REJECTED)
- **Relationships**:
  - OneToOne with User (seller has user account)
  - OneToOne with Address (business address)
  - OneToMany with Product (seller lists multiple products)
  - ManyToOne with Status

#### 10. **Status Entity**
- **Table**: `status`
- **Purpose**: Reference table for status values
- **Key Fields**:
  - `id` (PK): Auto-generated status ID
  - `value`: Status string (UNIQUE)
- **Predefined Values**:
  ```
  User Status: ACTIVE, INACTIVE, BLOCKED, DEACTIVATE, VERIFIED
  Order Status: PENDING, PACKING, DELIVERED, COMPLETED, CANCELLED
  Product Status: ACTIVE, INACTIVE
  Seller Status: ACTIVE, PENDING, APPROVED, REJECTED
  ```
- **Named Query**: `Status.findByValue` quickly retrieves status by name

#### 11. **DeliveryType Entity**
- **Table**: `delivery_types`
- **Purpose**: Defines shipping options and prices
- **Key Fields**:
  - `id` (PK): Auto-generated delivery type ID
  - `name`: Delivery method name (WITHIN_CITY, OUT_OF_CITY)
  - `price`: Cost of this delivery option
- **Values**:
  - WITHIN_CITY: 300.00 (local delivery)
  - OUT_OF_CITY: 500.00 (regional delivery)

#### 12. **Discount Entity**
- **Table**: `discount`
- **Purpose**: Coupon codes and discount management
- **Key Fields**:
  - `id` (PK): Auto-generated discount ID
  - `coupon_code`: Coupon code (UNIQUE)
  - `value`: Discount percentage/amount
  - `started_at`: Coupon valid start date
  - `expiered_at`: Coupon expiration date
- **Default Coupon**: 'DEFAULT' provides 0% discount (always available)

#### 13. **BaseEntity (Abstract)**
- **Purpose**: Base class for common audit fields
- **Key Fields**:
  - `created_at`: Timestamp when record created (auto-set, immutable)
  - `updated_at`: Timestamp when record last modified (auto-updated)
- **Mapped Superclass**: Inherited by User, Product, Stock, Seller
- **Annotation**: `@CreationTimestamp` auto-populates timestamps

---

## Core Components

### 1. Controllers (API Layer)

Controllers handle HTTP requests and return responses. They use Jersey annotations for RESTful endpoints.

#### **UserController** (`/api/users`)

**Endpoints**:

| Method | Path | Purpose | Auth |
|--------|------|---------|------|
| POST | `/api/users` | User registration | Public |
| POST | `/api/users/login` | User login | Public |
| GET | `/api/users/logout` | User logout | User |

**Key Logic**:
```java
createNewAccount()    // Validates and creates new user
userLogin()           // Authenticates user, creates session
logout()             // Invalidates user session
```

#### **ProductController** (`/api/products`)

**Responsibilities**:
- List all products with filtering
- Get product details
- Search products
- Filter by category

#### **CartController** (`/api/cart`)

**Responsibilities**:
- Add items to cart
- Remove items from cart
- Update quantities
- Get cart contents
- Clear cart

#### **CheckoutController** (`/api/checkout`)

**Responsibilities**:
- Process checkout
- Validate payment
- Create orders
- Handle PayHere payment integration

#### **ProfileController** (`/api/profile`)

**Responsibilities**:
- Get user profile
- Update user information
- Manage addresses
- View order history

#### **ContentController** (`/api/content`)

**Responsibilities**:
- Get static content
- Fetch cities
- Fetch categories
- Fetch delivery types

### 2. Services (Business Logic Layer)

Services contain business logic and database operations.

#### **UserService**

**Key Methods**:
```java
addNewUser(UserDTO)         // Register new user with validation
userLogin(UserDTO)          // Authenticate and create session
updateProfile(UserDTO)      // Update user information
addAddress(UserAddressDTO)  // Add delivery address
```

**Validation Rules**:
- Email: Must be valid format and unique
- Mobile: 10 digits, Sri Lankan format
- Password: Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char

#### **ProductService**

**Responsibilities**:
- Fetch products with pagination
- Filter by category
- Search functionality
- Get product stock information

#### **CartService**

**Responsibilities**:
- Add items to cart with stock validation
- Remove items
- Update quantities
- Calculate totals
- Clear cart

#### **OrderService**

**Responsibilities**:
- Create orders from cart items
- Update order status
- Calculate order totals
- Retrieve order history

#### **CheckoutService**

**Responsibilities**:
- Validate checkout data
- Calculate order amount
- Apply discounts
- Integrate with PayHere payment
- Create order records

**Payment Flow**:
```
User submits checkout
    ↓
CheckoutService validates
    ↓
Calculate total (items + delivery - discount)
    ↓
Call PayHereUtil for payment integration
    ↓
If payment successful: Create Order
    ↓
Send confirmation email
    ↓
Clear user's cart
```

### 3. Data Transfer Objects (DTOs)

DTOs are used to transfer data between layers without exposing entities directly.

#### **UserDTO**
```java
Fields: id, firstName, lastName, email, password, mobile, 
        confirmPassword, newPassword, sinceAt
```

#### **ProductDTO**
```java
Fields: id, title, description, price, quantity, categoryId, 
        images, stock information
```

#### **CartDTO**
```java
Fields: cartId, stockId, quantity, price, productTitle
```

#### **CheckoutRequestDTO**
```java
Fields: deliveryTypeId, discountCode, addressId, 
        paymentMethod, paymentDetails
```

**Purpose of DTOs**:
- Hide entity structure from clients
- Allow selective field exposure
- Decouple API from database schema
- Enable validation at API boundary
- Simplify data transformation

### 4. Middleware & Filters

Request interceptors for authentication and authorization.

#### **AuthAccessFilter**
- **Purpose**: Prevents logged-in users from accessing sign-up/sign-in pages
- **Paths**: `/sign_up.html`, `/sign_in.html`
- **Logic**: Redirect to home if already logged in

#### **AccessControlFilter**
- **Purpose**: Ensures user is logged in before accessing protected pages
- **Paths**: `/my_account.html`, `/checkout.html`
- **Logic**: Redirect to login if not authenticated

#### **AuthFilter**
- **Purpose**: Validates API requests have valid session
- **For**: API endpoints marked with `@IsUser` annotation

#### **IsUser Annotation**
```java
@IsUser  // Applied to methods requiring authentication
public Response protectedEndpoint(@Context HttpServletRequest request)
```

### 5. Utilities

#### **HibernateUtil**
- **Purpose**: Manages SessionFactory lifecycle
- **Pattern**: Lazy initialization singleton
- **Features**:
  - Thread-safe
  - Fallback to H2 if MySQL unavailable
  - Automatic schema updates

#### **AppUtil**
- **Purpose**: Common application utilities
- **Provides**:
  - GSON instance for JSON serialization
  - String manipulation helpers
  - Email utilities

#### **PayHereUtil**
- **Purpose**: Integration with PayHere payment gateway
- **Merchant Credentials**:
  ```
  Merchant ID: 1224621
  Merchant Secret: [encoded]
  Return URL: App callback endpoint
  ```

#### **Env**
- **Purpose**: Environment-specific configuration
- **Reads from**: `app.properties`
- **Properties**:
  ```
  mail.host, mail.port, mail.username, mail.password
  app.mail, app.name, app.url
  payhere.merchant.id, payhere.merchant.secret
  ```

---

## API Endpoints

### Authentication Endpoints

```
POST /api/users
├── Request Body: {firstName, lastName, email, password, confirmPassword, mobile}
├── Response: {status: boolean, message: string, userId: int}
└── Validation: Email unique, password strength, mobile format

POST /api/users/login
├── Request Body: {email, password}
├── Response: {status: boolean, message: string, user: UserDTO}
└── Creates: HTTP Session with user object

GET /api/users/logout (@IsUser)
├── Response: 202 ACCEPTED or 400 BAD_REQUEST
└── Destroys: HTTP Session
```

### Product Endpoints

```
GET /api/products
├── Query Params: ?page=1&size=10&category=1&search=term
└── Response: List<ProductDTO> with pagination

GET /api/products/{id}
├── Response: ProductDTO with full details and stock info
└── Includes: Images, seller info, available quantities

GET /api/products/categories
└── Response: List<CategoryDTO>
```

### Cart Endpoints

```
POST /api/cart (@IsUser)
├── Request Body: {stockId, quantity}
└── Creates: Cart entry for user

DELETE /api/cart/{cartId} (@IsUser)
└── Removes: Item from cart

GET /api/cart (@IsUser)
└── Response: List<CartDTO> for logged-in user

PUT /api/cart/{cartId} (@IsUser)
├── Request Body: {quantity}
└── Updates: Quantity of cart item

DELETE /api/cart/clear (@IsUser)
└── Clears: All items from user's cart
```

### Checkout Endpoints

```
POST /api/checkout (@IsUser)
├── Request Body: CheckoutRequestDTO
├── Processes: Payment via PayHere
├── Creates: Order and OrderItems
└── Response: {orderId, status, paymentReference}

GET /api/checkout/delivery-types
└── Response: List<DeliveryTypeDTO> with pricing
```

### Profile Endpoints

```
GET /api/profile (@IsUser)
└── Response: UserDTO with full profile

PUT /api/profile (@IsUser)
├── Request Body: {firstName, lastName, mobile, newPassword}
└── Updates: User profile information

POST /api/profile/address (@IsUser)
├── Request Body: UserAddressDTO
└── Creates: New address for user

GET /api/profile/addresses (@IsUser)
└── Response: List<UserAddressDTO>

GET /api/profile/orders (@IsUser)
└── Response: List of user's orders with status
```

### Content Endpoints

```
GET /api/content/cities
└── Response: List<CityDTO> for address selection

GET /api/content/categories
└── Response: List<CategoryDTO> for product filtering

GET /api/content/delivery-types
└── Response: List<DeliveryTypeDTO> with pricing
```

---

## Data Flow

### User Registration Flow

```
1. Frontend: User submits registration form (sign_up.html)
   ↓
2. sign_up.js: Validates locally, sends POST to /api/users
   ↓
3. UserController: Receives JSON, creates UserDTO
   ↓
4. UserService.addNewUser():
   a) Validates all fields (email format, password strength, etc.)
   b) Checks email doesn't already exist (Named Query)
   c) Creates User entity
   d) Sets Status to ACTIVE
   e) Saves to database via Hibernate
   ↓
5. Response: {status: true, message: "User created", userId: 123}
   ↓
6. Frontend: Redirects to login page
```

### Login Flow

```
1. Frontend: User submits login (sign_in.html)
   ↓
2. sign_in.js: Sends POST to /api/users/login with credentials
   ↓
3. UserController: Receives email/password
   ↓
4. UserService.userLogin():
   a) Queries User by email (Named Query)
   b) Compares passwords
   c) If match: Creates HttpSession, stores User object
   d) If fail: Returns error message
   ↓
5. Response: User data or error
   ↓
6. Frontend: Stores authentication state, redirects to home
```

### Shopping Flow

```
1. Browse Products
   ├─ shop.html loads
   ├─ shop.js calls GET /api/products
   ├─ ProductService retrieves from database
   └─ Display products with images

2. View Product Details
   ├─ User clicks product
   ├─ single-product-view.html loads
   ├─ Calls GET /api/products/{id}
   ├─ Displays: Full description, images, available stocks, prices
   └─ Shows: Seller info, ratings

3. Add to Cart
   ├─ User selects quantity, clicks "Add to Cart"
   ├─ cart.js sends POST /api/cart
   ├─ CartService validates stock availability
   ├─ Creates Cart entity linking User + Stock + Quantity
   ├─ Saves to database
   └─ Frontend: Shows success notification

4. View Cart
   ├─ User navigates to cart (checkout.html)
   ├─ Calls GET /api/cart
   ├─ CartService retrieves cart items
   ├─ Calculates total price with stock pricing
   └─ Display: Items, quantities, prices, subtotal

5. Checkout
   ├─ User reviews cart, selects delivery
   ├─ User fills/selects delivery address
   ├─ Submits POST /api/checkout with payment info
   ├─ CheckoutService:
   │   a) Validates address exists
   │   b) Calculates total: items + delivery - discount
   │   c) Calls PayHereUtil.initiatePayment()
   │   d) Returns payment redirect URL
   ├─ Frontend: Redirects to PayHere payment gateway
   ├─ PayHere processes payment
   ├─ Callback to /api/checkout/callback
   ├─ CheckoutService creates Order + OrderItems
   ├─ Clears user's cart
   └─ Sends confirmation email

6. Order History
   ├─ User views my_account.html
   ├─ my_account.js calls GET /api/profile/orders
   ├─ OrderService retrieves user's orders with items
   └─ Display: Order ID, date, status, items, total
```

### Admin/Seller Operations

```
Product Approval (Admin):
1. Admin views pending products (status = PENDING)
2. Approves/Rejects (updates status to APPROVED/REJECTED)
3. Approved products become visible in Shop

Stock Management (Seller):
1. Seller adds product (creates Product entity)
2. Seller adds stock variant (creates Stock entity with price, quantity, expiry)
3. Stock appears in catalog
4. When ordered: Quantity decreases, available stock shown

Payment Processing:
1. Order proceeds to PayHere
2. Customer completes payment
3. PayHere notifies app via callback
4. Order status changes to COMPLETED
5. Payment confirmation email sent
6. Stock quantities updated
```

---

## Setup & Deployment

### Prerequisites

```
1. Java Development Kit (JDK) 17 or higher
2. Maven 3.6+
3. MySQL Server 5.7+
4. Git (for version control)
```

### Database Setup

#### Create Database

```sql
CREATE DATABASE aesthetica;
CREATE USER 'aesthetica_user'@'localhost' IDENTIFIED BY 'mysql2006';
GRANT ALL PRIVILEGES ON aesthetica.* TO 'aesthetica_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Initialize Schema

```bash
# Run migration SQL (Hibernate will auto-create tables)
mysql -u aesthetica_user -p aesthetica < src/main/resources/db_migration_2026_02_20.sql

# Seed initial data
mysql -u aesthetica_user -p aesthetica < src/main/resources/db_seed_data.sql
```

**Or** let Hibernate auto-create (configuration already set to `update`):
- First run: Tables will be created automatically
- Subsequent runs: Schema updates as needed

### Build Instructions

```bash
# Navigate to project directory
cd /mnt/winterfell/College/Curriculam/WEB2/Aesthetica

# Clean and build
mvn clean install

# Compile only (skip tests)
mvn clean compile

# Create WAR file
mvn package
```

### Run Instructions

#### Option 1: Direct Execution (Embedded Tomcat)

```bash
# Compile
mvn compile

# Run Main class
mvn exec:java -Dexec.mainClass="com.aesthetica.Main"

# Application starts on http://localhost:8080/aesthetica
```

#### Option 2: IDE Execution

1. Open project in IntelliJ IDEA or NetBeans
2. Build project (Ctrl+F9 in IntelliJ)
3. Run Main.java class
4. Access: http://localhost:8080/aesthetica

#### Option 3: Deploy WAR

```bash
# Generate WAR
mvn package

# Copy to Tomcat
cp target/aesthetica.war /path/to/tomcat/webapps/

# Tomcat auto-deploys WAR
```

### Configuration

#### Environment Variables

Edit `src/main/resources/app.properties`:

```properties
# Email Configuration
mail.host=smtp.gmail.com              # Your mail server
mail.port=587
mail.username=your-email@gmail.com
mail.password=your-app-password

# Application Settings
app.mail=your-app-email@gmail.com
app.name=aesthetica
app.url=http://localhost:8080/aesthetica

# PayHere Payment Gateway
payhere.merchant.id=YOUR_MERCHANT_ID
payhere.merchant.secret=YOUR_MERCHANT_SECRET
```

#### Database Connection

Edit `src/main/resources/hibernate.cfg.xml`:

```xml
<property name="hibernate.connection.url">
  jdbc:mysql://localhost:3306/aesthetica?useSSL=false
</property>
<property name="hibernate.connection.username">aesthetica_user</property>
<property name="hibernate.connection.password">mysql2006</property>
```

### Port Configuration

Default port is `8080`. Context path is `/aesthetica`.

To change in `Main.java`:

```java
private static final String CONTEXT_PATH = "/aesthetica";  // Change context
tomcat.setPort(8080);  // Change port
```

### Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port 8080 already in use** | Change port in Main.java or kill process on 8080 |
| **Database connection fails** | Check MySQL running, verify credentials in hibernate.cfg.xml |
| **Compilation errors** | Run `mvn clean compile`, check Java version (17+) |
| **No tables created** | Ensure MySQL user has TABLE creation privileges |
| **Email sending fails** | Update app.properties with correct mail credentials |

---

## File-by-File Explanation

### Configuration Files

#### **pom.xml**
**Purpose**: Maven Project configuration
- Defines project metadata and dependencies
- Specifies Java 17 as target version
- Lists all third-party libraries
- Configures Maven plugins (war, clean, compiler)
- **Key Dependencies**:
  - Tomcat Embedded (server)
  - Jersey (REST API)
  - Hibernate (ORM)
  - MySQL Connector (database)
  - Jakarta Servlet/Mail/Activation
  - GSON (JSON)
  - Commons IO (file handling)

#### **Aesthetica.iml**
**Purpose**: IntelliJ IDEA project file
- Stores project structure, classpaths, IDE settings
- Not edited manually; auto-generated by IDE

#### **nb-configuration.xml**
**Purpose**: NetBeans IDE configuration
- Stores NetBeans-specific project settings
- Auto-generated by NetBeans

### Startup Files

#### **Main.java**
**Purpose**: Application entry point and server startup

**Key Responsibilities**:
1. Create embedded Tomcat instance
2. Configure Jersey servlet for API routing
3. Register middleware/filters
4. Start Tomcat server
5. Log startup information

**Startup Sequence**:
```java
Tomcat tomcat = new Tomcat();
tomcat.setPort(8080);

Context context = tomcat.addWebapp(
    "/aesthetica",                    // Context path
    new File("src/main/webapp")   // Web content directory
);

// Register Jersey servlet for API
Tomcat.addServlet(context, "JerseyServlet", new ServletContainer(new AppConfig()));
context.addServletMappingDecoded("/api/*", "JerseyServlet");

// Register filters
context.addApplicationListener(ContextPathListener.class.getName());
context.addApplicationListener(AuthAccessFilter.class.getName());

tomcat.start();  // Start server
tomcat.getServer().await();  // Keep running
```

### Configuration Classes

#### **AppConfig.java**
**Purpose**: Jersey Framework configuration
```java
public AppConfig extends ResourceConfig {
    public AppConfig() {
        packages("com.aesthetica.controller");  // Scan for REST controllers
        packages("com.aesthetica.middleware");  // Scan for filters
        register(MultiPartFeature.class);       // Enable file uploads
    }
}
```

**What It Does**:
- Registers all classes in `controller` package as REST resources
- Enables multipart form data (file uploads)
- Makes Jersey aware of middleware components

### Entity Files (JPA/Hibernate)

All entity files use Jakarta Persistence annotations (modern Java EE standard).

**Common Patterns**:

```java
@Entity                    // Marks class as persistent
@Table(name = "table")    // Database table name
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String field;
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;
    
    // Getters/Setters...
}
```

**Relationship Annotations**:
- `@OneToOne`: Single parent, single child
- `@OneToMany`: Single parent, multiple children
- `@ManyToOne`: Multiple parents, single parent
- `@ManyToMany`: Multiple parents, multiple children
- `mappedBy`: Marks inverse side of relationship
- `fetch = FetchType.LAZY/EAGER`: Loading strategy

### Service Files

**Pattern**: Each service handles one domain (User, Product, Cart, Order, etc.)

**Lifecycle**:
1. Controller receives HTTP request
2. Controller creates Service instance
3. Service performs validation
4. Service executes business logic
5. Service interacts with database via Hibernate
6. Service returns result
7. Controller converts to JSON response

**Example (UserService.addNewUser)**:
```java
public String addNewUser(UserDTO userDTO) {
    // 1. Validate input
    if (validation fails) {
        return error JSON
    }
    
    // 2. Check business rules (email unique)
    Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
    User existing = hibernateSession.createNamedQuery("User.getByEmail", User.class)
        .setParameter("email", userDTO.getEmail())
        .getSingleResultOrNull();
    
    if (existing != null) {
        return "Email already registered"
    }
    
    // 3. Create entity
    User user = new User();
    user.setEmail(userDTO.getEmail());
    // ... set other fields
    
    // 4. Set default status
    Status status = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
        .setParameter("value", "ACTIVE")
        .getSingleResultOrNull();
    user.setStatus(status);
    
    // 5. Persist to database
    Transaction tx = hibernateSession.beginTransaction();
    hibernateSession.persist(user);
    tx.commit();
    hibernateSession.close();
    
    // 6. Return success response as JSON
    return "{\"status\": true, \"userId\": " + user.getId() + "}"
}
```

### Middleware/Filter Files

#### **AuthAccessFilter.java**
```java
@Path("/")
public class AuthAccessFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext context) {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user != null && protected route) {
            // Redirect to home - already logged in
        }
    }
}
```

#### **AccessControlFilter.java**
```java
@Path("/")
public class AccessControlFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext context) {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user == null && protected route) {
            // Redirect to login - not authenticated
        }
    }
}
```

### DTO Files

DTOs transform data for API transmission. Example:

```java
public class CartDTO {
    private int cartId;
    private String productTitle;
    private int quantity;
    private Double price;
    private Double totalPrice;
    private String sellerName;
    
    // Getters/Setters (simplified data for frontend)
}
```

**vs Entity**:
- Entity: Contains ALL fields, relationships, business rules
- DTO: Contains ONLY fields needed by client, relationships are IDs

### Utility Files

#### **HibernateUtil.java**
```java
private static volatile SessionFactory sessionFactory = null;

public static synchronized SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
        try {
            // Try MySQL
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (HibernateException e) {
            // Fallback to H2 in-memory
            Configuration fallback = new Configuration().configure();
            fallback.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            fallback.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            fallback.setProperty("hibernate.connection.url", "jdbc:h2:mem:aesthetica");
            sessionFactory = fallback.buildSessionFactory();
        }
    }
    return sessionFactory;
}
```

**Thread-Safe**: Using `synchronized` and `volatile` for lazy initialization in multi-threaded environment.

### HTML/Frontend Files

Each HTML file represents a page/view:

- **index.html**: Home page - featured products, categories
- **shop.html**: Product listing page - filtering, search, pagination
- **single-product-view.html**: Product detail page - images, description, reviews, add to cart
- **checkout.html**: Order review and payment - address selection, delivery option, payment
- **my_account.html**: User profile - personal info, addresses, order history
- **sign_up.html**: Registration form
- **sign_in.html**: Login form
- **all_categories.html**: Category browse page
- **contact.html**: Contact/support page

### JavaScript Files

Each page has corresponding JS file handling:
- Event listeners
- Form validation
- API calls (fetch)
- DOM manipulation
- Notifications

**Pattern**:
```javascript
// sign_up.js
document.getElementById('submit').addEventListener('click', async (e) => {
    e.preventDefault();
    
    let formData = {
        firstName: document.getElementById('firstName').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        // ... other fields
    };
    
    // Validate
    if (!validateEmail(formData.email)) {
        showError("Invalid email");
        return;
    }
    
    // Call API
    const response = await fetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    });
    
    const result = await response.json();
    
    if (result.status) {
        showSuccess("Account created!");
        window.location = '/sign_in.html';
    } else {
        showError(result.message);
    }
});
```

### SQL Migration Files

#### **db_migration_2026_02_20.sql**
**Purpose**: Database schema updates and migrations

**Changes**:
1. Ensure required statuses exist (ACTIVE, PENDING, etc.)
2. Set ACTIVE as default user status
3. Remove old email verification columns
4. Enforce safe defaults for product dimensions (1.0)
5. Make seller_id optional for products

**Why**: Accommodates evolution of requirements without breaking existing data.

#### **db_seed_data.sql**
**Purpose**: Initial data population

**Data Inserted**:
- Status values (ACTIVE, INACTIVE, PENDING, etc.)
- Delivery types (WITHIN_CITY: 300, OUT_OF_CITY: 500)
- Default discount (0%)
- Cities (Colombo, Kandy, Galle, Jaffna, Kurunegala, etc.)
- Default categories

**Safety**: All inserts use `NOT EXISTS` clauses to avoid duplicates.

### Resource Properties

#### **app.properties**
**Purpose**: Application configuration and credentials

**Sections**:
1. Email Configuration - Mailtrap credentials for testing
2. Application Settings - App name, URL, email
3. PayHere Integration - Merchant credentials for payment processing

---

## Summary: How Components Connect

```
┌──────────────────────────────────────────────────────┐
│                   BROWSER/CLIENT                      │
│            (HTML, CSS, JavaScript)                    │
└────────────────────┬─────────────────────────────────┘
                     │
                     │ HTTP Requests (JSON)
                     │
┌────────────────────▼─────────────────────────────────┐
│              JERSEY REST CONTROLLERS                  │
│  UserController, ProductController, CartController   │
│                      ▼                                │
│            RequestReceived → DTO Mapping             │
└────────────────────┬─────────────────────────────────┘
                     │
                     │ Service calls
                     │
┌────────────────────▼─────────────────────────────────┐
│            BUSINESS LOGIC SERVICES                    │
│  UserService, ProductService, CartService, etc.      │
│         ▼                                             │
│    Validation Logic                                   │
│    Business Rules Enforcement                        │
│    Database Operations                               │
└────────────────────┬─────────────────────────────────┘
                     │
                     │ Hibernate Sessions
                     │
┌────────────────────▼─────────────────────────────────┐
│              HIBERNATE ORM LAYER                      │
│    Maps Java Objects ↔ Database Tables               │
│    EntityManager, Session, Query Execution           │
└────────────────────┬─────────────────────────────────┘
                     │
                     │ SQL Queries
                     │
┌────────────────────▼─────────────────────────────────┐
│                MYSQL DATABASE                         │
│  Tables: users, products, orders, carts, etc.        │
│  Relationships: Foreign Keys, Indexes                │
└──────────────────────────────────────────────────────┘
```

---

## Code Quality & Best Practices

### Design Patterns Used

1. **Singleton Pattern**: HibernateUtil (single SessionFactory instance)
2. **DAO Pattern**: Services act as data access abstraction
3. **DTO Pattern**: Separate data transfer objects from entities
4. **MVC Pattern**: Controllers + Views + Services = separation of concerns
5. **Lazy Initialization**: SessionFactory created only when needed
6. **Strategy Pattern**: Multiple delivery types for different strategies

### Naming Conventions

**Packages**:
- `com.aesthetica.entity` - JPA entities
- `com.aesthetica.service` - Business logic
- `com.aesthetica.controller` - REST endpoints
- `com.aesthetica.dto` - Data transfer objects
- `com.aesthetica.util` - Utility classes
- `com.aesthetica.middleware` - Filters/interceptors

**Classes**:
- Entities: Singular nouns (User, Product, Order)
- Services: Singular noun + "Service" (UserService, ProductService)
- Controllers: Plural nouns + "Controller" (UsersController, ProductsController)
- Filters: Descriptive name ending in "Filter" (AuthAccessFilter)
- DTOs: Singular noun + "DTO" (UserDTO, ProductDTO)

**Database**:
- Tables: Plural (users, products, orders)
- Columns: snake_case (first_name, user_id)
- Foreign Keys: table_id pattern (user_id, product_id)

### Error Handling

Services catch Hibernate exceptions and return user-friendly error messages as JSON responses.

### Validation

Input validation occurs at multiple layers:
1. Frontend JavaScript validation (UX feedback)
2. Service layer validation (email format, password strength, mobile format)
3. Database constraints (unique, not null, length, etc.)

### Logging

`System.out.println()` and `System.err.println()` used for debugging. Hibernate logs SQL queries when `hibernate.show_sql=true`.

---

## Conclusion

Aesthetica is a well-structured, modern Java web application demonstrating enterprise-level software architecture. It successfully combines:

- **Clean Architecture**: Separation of concerns with distinct layers
- **Modern Frameworks**: Latest versions of Hibernate, Jersey, and Jakarta
- **Database Design**: Normalized schema with proper relationships
- **Scalability**: Ready for multi-seller marketplace scaling
- **Maintainability**: Clear code organization and naming conventions
- **User Experience**: Responsive frontend with comprehensive functionality

The project serves as an excellent reference for building full-stack Java applications with professional architecture and practices.

---

**Documentation Generated**: May 29, 2026
**Project Version**: 1.0
**Technology Stack**: Java 17, Hibernate 6, Jersey 3, MySQL 5.7+

