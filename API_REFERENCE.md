# Aesthetica - REST API Reference Guide

## Complete API Documentation

Comprehensive reference for all REST API endpoints in Aesthetica.

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Authentication](#authentication)
3. [Common Response Formats](#common-response-formats)
4. [Error Handling](#error-handling)
5. [User Endpoints](#user-endpoints)
6. [Product Endpoints](#product-endpoints)
7. [Cart Endpoints](#cart-endpoints)
8. [Order & Checkout Endpoints](#order--checkout-endpoints)
9. [Profile Endpoints](#profile-endpoints)
10. [Content Endpoints](#content-endpoints)
11. [Rate Limiting & Best Practices](#rate-limiting--best-practices)

---

## API Overview

### URL Structure

```
http://localhost:8080/aesthetica/api/[endpoint]
```

**Components**:
- **Protocol**: HTTP (localhost) / HTTPS (production)
- **Host**: localhost
- **Port**: 8080
- **Context Path**: /aesthetica
- **API Base**: /api

### Request/Response Format

All endpoints use **JSON** for request and response bodies.

**Request Headers**:
```
Content-Type: application/json
Accept: application/json
```

**Response Headers**:
```
Content-Type: application/json; charset=UTF-8
```

### HTTP Methods

| Method | Purpose | Idempotent |
|--------|---------|-----------|
| GET | Retrieve data | ✓ |
| POST | Create new resource | ✗ |
| PUT | Update existing resource | ✓ |
| DELETE | Remove resource | ✓ |

---

## Authentication

### Session-Based Authentication

Aesthetica uses HTTP sessions for authentication.

#### Login Process

1. User submits credentials to `/api/users/login`
2. Server validates and creates HTTP session
3. Session stored in browser cookies
4. Subsequent requests automatically include session cookie
5. Filters check session validity on protected endpoints

#### Session Management

```bash
# After successful login
# Browser receives Set-Cookie header:
Set-Cookie: JSESSIONID=ABC123DEF456; Path=/aesthetica; HttpOnly

# All subsequent requests include:
Cookie: JSESSIONID=ABC123DEF456
```

#### Protected Endpoints

Endpoints marked with `@IsUser` annotation require valid session.

```bash
# Without authentication:
curl http://localhost:8080/aesthetica/api/profile

Response: 401 Unauthorized (or redirect to login)

# With authentication (browser/session):
curl -H "Cookie: JSESSIONID=..." http://localhost:8080/aesthetica/api/profile

Response: 200 OK with user data
```

#### Logout

```bash
GET /api/users/logout

Response: 202 ACCEPTED
Effect: Session invalidated, JSESSIONID cleared
```

### Authorization Levels

| Level | Access | Endpoints |
|-------|--------|-----------|
| Public | No login required | Register, Login, Browse products, Content |
| User | Login required | Cart, Orders, Profile, Checkout |
| Admin | Admin account | (Not implemented in current version) |

---

## Common Response Formats

### Success Response (JSON)

```json
{
  "status": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "firstName": "John",
    "email": "john@example.com"
  }
}
```

**Standard Fields**:
- `status` (boolean): true for success, false for failure
- `message` (string): Human-readable message
- `data` (object): Response payload (optional)

### List Response

```json
{
  "status": true,
  "message": "Resources retrieved",
  "data": [
    { "id": 1, "name": "Electronics" },
    { "id": 2, "name": "Books" }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 25,
    "pages": 3
  }
}
```

### Error Response

```json
{
  "status": false,
  "message": "Email already registered",
  "error": "DUPLICATE_EMAIL"
}
```

### Status Codes

| Code | Meaning | Common Reasons |
|------|---------|----------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created |
| 202 | Accepted | Request accepted (logout) |
| 400 | Bad Request | Invalid data, missing fields |
| 401 | Unauthorized | Not logged in |
| 403 | Forbidden | Access denied |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate email, item already in cart |
| 500 | Server Error | Database error, unexpected exception |

---

## Error Handling

### Validation Errors

```json
{
  "status": false,
  "message": "Email must be valid format",
  "validationErrors": [
    {
      "field": "email",
      "message": "Invalid email format"
    },
    {
      "field": "password",
      "message": "Password must contain: 1 uppercase, 1 digit, 1 special character"
    }
  ]
}
```

### Business Logic Errors

```json
{
  "status": false,
  "message": "Stock not available",
  "errorCode": "OUT_OF_STOCK",
  "details": {
    "availableQty": 5,
    "requestedQty": 10
  }
}
```

### Error Recovery

```javascript
// Frontend error handling
fetch('/api/users', { method: 'POST', body: JSON.stringify(data) })
  .then(res => res.json())
  .then(result => {
    if (result.status) {
      // Success
      console.log('User created');
    } else {
      // Handle error
      console.error(result.message);
      displayValidationErrors(result.validationErrors);
    }
  })
  .catch(err => console.error('Network error:', err));
```

---

## User Endpoints

### Register New User

**Endpoint**: `POST /api/users`

**Authentication**: None (public)

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass@1234",
  "confirmPassword": "SecurePass@1234",
  "mobile": "0712345678"
}
```

**Validation Rules**:

| Field | Rules |
|-------|-------|
| firstName | Required, not empty |
| lastName | Required, not empty |
| email | Required, valid format, unique in database |
| password | Required, 8+ chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char |
| confirmPassword | Must match password |
| mobile | Required, 10 digits, valid Sri Lankan format |

**Response - Success (201)**:
```json
{
  "status": true,
  "message": "User created successfully",
  "data": {
    "userId": 42,
    "email": "john@example.com"
  }
}
```

**Response - Failure (400)**:
```json
{
  "status": false,
  "message": "Email already registered",
  "validationErrors": [
    {
      "field": "email",
      "message": "This email is already in use"
    }
  ]
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/aesthetica/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com",
    "password": "Pass@1234",
    "confirmPassword": "Pass@1234",
    "mobile": "0712345679"
  }'
```

---

### User Login

**Endpoint**: `POST /api/users/login`

**Authentication**: None (public)

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "SecurePass@1234"
}
```

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Login successful",
  "data": {
    "id": 42,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "mobile": "0712345678",
    "createdAt": "2026-05-01T10:30:00"
  }
}
```

**Response - Failure (400)**:
```json
{
  "status": false,
  "message": "Invalid email or password"
}
```

**Side Effects**:
- Browser receives `Set-Cookie: JSESSIONID=...` header
- Session created on server with user data
- Subsequent requests must include session cookie

**Example cURL**:
```bash
curl -X POST http://localhost:8080/aesthetica/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass@1234"
  }'

# -c cookies.txt: Save cookies to file for future requests
```

---

### User Logout

**Endpoint**: `GET /api/users/logout`

**Authentication**: Required (@IsUser)

**Request Body**: None

**Response - Success (202)**:
```
[Empty body]
Response Status: ACCEPTED
```

**Response - Failure (400)**:
```json
{
  "status": false,
  "message": "No active session"
}
```

**Side Effects**:
- Session invalidated on server
- Session cookie cleared in browser
- User must login again for protected endpoints

**Example cURL**:
```bash
curl -X GET http://localhost:8080/aesthetica/api/users/logout \
  -b cookies.txt  # Use saved session cookie
```

---

## Product Endpoints

### Get All Products

**Endpoint**: `GET /api/products`

**Authentication**: None (public)

**Query Parameters**:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | int | 1 | Page number (1-indexed) |
| size | int | 10 | Items per page (max 100) |
| categoryId | int | - | Filter by category ID |
| search | string | - | Search in title/description |
| sortBy | string | created_at | Sort field |
| order | string | DESC | ASC or DESC |

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Products retrieved",
  "data": [
    {
      "id": 1,
      "title": "Laptop Pro",
      "description": "High-performance laptop...",
      "categoryId": 5,
      "categoryName": "Electronics",
      "images": ["laptop1.jpg", "laptop2.jpg"],
      "stocks": [
        {
          "id": 101,
          "price": 150000,
          "quantity": 25,
          "discount": 0
        }
      ],
      "seller": {
        "id": 10,
        "companyName": "TechStore",
        "companyEmail": "contact@techstore.com"
      },
      "createdAt": "2026-01-15T08:00:00"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "total": 156,
    "pages": 16
  }
}
```

**Example cURL**:
```bash
# Get page 2 with 20 items per page
curl "http://localhost:8080/aesthetica/api/products?page=2&size=20"

# Filter by category (Electronics = 5)
curl "http://localhost:8080/aesthetica/api/products?categoryId=5"

# Search for products
curl "http://localhost:8080/aesthetica/api/products?search=laptop"

# Combined
curl "http://localhost:8080/aesthetica/api/products?categoryId=5&search=laptop&page=1&size=12"
```

---

### Get Product Details

**Endpoint**: `GET /api/products/{productId}`

**Authentication**: None (public)

**Path Parameters**:
- `productId` (int): Unique product identifier

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Product retrieved",
  "data": {
    "id": 1,
    "title": "Laptop Pro",
    "description": "13-inch FHD display, Intel i7, 16GB RAM, 512GB SSD",
    "weight": 1.2,
    "length": 30.0,
    "width": 21.0,
    "height": 1.5,
    "categoryId": 5,
    "categoryName": "Electronics",
    "images": [
      "system/products/laptop_1.jpg",
      "system/products/laptop_2.jpg"
    ],
    "stocks": [
      {
        "id": 101,
        "price": 150000,
        "quantity": 25,
        "discount": 0,
        "status": "ACTIVE",
        "manufacturedDate": "2025-12-01",
        "expiryDate": "2028-12-01"
      }
    ],
    "seller": {
      "id": 10,
      "companyName": "TechStore",
      "companyMobile": "0711234567",
      "companyEmail": "contact@techstore.com",
      "status": "APPROVED"
    },
    "createdAt": "2026-01-15T08:00:00",
    "ratings": {
      "average": 4.5,
      "count": 120
    }
  }
}
```

**Response - Not Found (404)**:
```json
{
  "status": false,
  "message": "Product not found"
}
```

**Example cURL**:
```bash
curl http://localhost:8080/aesthetica/api/products/1
```

---

## Cart Endpoints

### Add Item to Cart

**Endpoint**: `POST /api/cart`

**Authentication**: Required (@IsUser)

**Request Body**:
```json
{
  "stockId": 101,
  "quantity": 2
}
```

**Validation**:
- Stock must exist and be ACTIVE
- Quantity must be positive integer
- Quantity must not exceed available stock

**Response - Success (201)**:
```json
{
  "status": true,
  "message": "Item added to cart",
  "data": {
    "cartId": 250,
    "productTitle": "Laptop Pro",
    "quantity": 2,
    "price": 150000,
    "totalPrice": 300000
  }
}
```

**Response - Failure (400)**:
```json
{
  "status": false,
  "message": "Requested quantity exceeds available stock",
  "data": {
    "availableQty": 3,
    "requestedQty": 5
  }
}
```

**Example cURL**:
```bash
curl -X POST http://localhost:8080/aesthetica/api/cart \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "stockId": 101,
    "quantity": 2
  }'
```

---

### View Cart

**Endpoint**: `GET /api/cart`

**Authentication**: Required (@IsUser)

**Request Body**: None

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Cart retrieved",
  "data": [
    {
      "cartId": 250,
      "stockId": 101,
      "productId": 1,
      "productTitle": "Laptop Pro",
      "quantity": 2,
      "price": 150000,
      "totalPrice": 300000,
      "images": ["laptop_1.jpg"],
      "seller": "TechStore"
    },
    {
      "cartId": 251,
      "stockId": 102,
      "productId": 2,
      "productTitle": "Wireless Mouse",
      "quantity": 3,
      "price": 2500,
      "totalPrice": 7500,
      "images": ["mouse_1.jpg"],
      "seller": "ElectroStore"
    }
  ],
  "summary": {
    "itemCount": 2,
    "itemsQuantity": 5,
    "subtotal": 307500
  }
}
```

**Response - Empty Cart (200)**:
```json
{
  "status": true,
  "message": "Cart is empty",
  "data": [],
  "summary": {
    "itemCount": 0,
    "itemsQuantity": 0,
    "subtotal": 0
  }
}
```

**Example cURL**:
```bash
curl -X GET http://localhost:8080/aesthetica/api/cart -b cookies.txt
```

---

### Update Cart Item

**Endpoint**: `PUT /api/cart/{cartId}`

**Authentication**: Required (@IsUser)

**Path Parameters**:
- `cartId` (int): Cart item ID to update

**Request Body**:
```json
{
  "quantity": 5
}
```

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Cart updated",
  "data": {
    "cartId": 250,
    "productTitle": "Laptop Pro",
    "quantity": 5,
    "totalPrice": 750000
  }
}
```

**Example cURL**:
```bash
curl -X PUT http://localhost:8080/aesthetica/api/cart/250 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"quantity": 5}'
```

---

### Remove from Cart

**Endpoint**: `DELETE /api/cart/{cartId}`

**Authentication**: Required (@IsUser)

**Path Parameters**:
- `cartId` (int): Cart item ID to remove

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Item removed from cart"
}
```

**Example cURL**:
```bash
curl -X DELETE http://localhost:8080/aesthetica/api/cart/250 \
  -b cookies.txt
```

---

### Clear Cart

**Endpoint**: `DELETE /api/cart/clear`

**Authentication**: Required (@IsUser)

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Cart cleared successfully"
}
```

**Example cURL**:
```bash
curl -X DELETE http://localhost:8080/aesthetica/api/cart/clear \
  -b cookies.txt
```

---

## Order & Checkout Endpoints

### Get Delivery Types

**Endpoint**: `GET /api/checkout/delivery-types`

**Authentication**: None (public) or Required for checkout

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Delivery types retrieved",
  "data": [
    {
      "id": 1,
      "name": "WITHIN_CITY",
      "price": 300.00,
      "description": "Local delivery (2-3 days)"
    },
    {
      "id": 2,
      "name": "OUT_OF_CITY",
      "price": 500.00,
      "description": "Regional delivery (5-7 days)"
    }
  ]
}
```

---

### Create Order (Checkout)

**Endpoint**: `POST /api/checkout`

**Authentication**: Required (@IsUser)

**Request Body**:
```json
{
  "deliveryTypeId": 1,
  "discountCode": "DEFAULT",
  "addressId": 5,
  "paymentMethod": "card",
  "paymentDetails": {
    "cardNumber": "4111111111111111",
    "expiryMonth": "12",
    "expiryYear": "2026",
    "cvv": "123"
  }
}
```

**Process**:
1. Validate cart not empty
2. Validate address exists and belongs to user
3. Validate delivery type exists
4. Calculate total: cart items + delivery - discount
5. Call PayHere payment gateway
6. If payment successful:
   - Create Order record
   - Create OrderItems from cart
   - Update Stock quantities
   - Clear user's cart
7. Send confirmation email

**Response - Success (201)**:
```json
{
  "status": true,
  "message": "Order created successfully",
  "data": {
    "orderId": 1000,
    "orderNumber": "ORD-2026-05-29-001000",
    "total": 307800,
    "status": "COMPLETED",
    "deliveryType": "WITHIN_CITY",
    "estimatedDelivery": "2026-06-01",
    "items": [
      {
        "id": 1,
        "productTitle": "Laptop Pro",
        "quantity": 2,
        "price": 150000,
        "subtotal": 300000
      }
    ],
    "paymentReference": "PAY123456789",
    "createdAt": "2026-05-29T14:30:00"
  }
}
```

**Response - Failure if out of stock (400)**:
```json
{
  "status": false,
  "message": "Item no longer available",
  "data": {
    "item": "Laptop Pro",
    "requestedQty": 2,
    "availableQty": 1
  }
}
```

**Response - Payment failed (400)**:
```json
{
  "status": false,
  "message": "Payment declined. Please try another card.",
  "paymentError": "CARD_DECLINED"
}
```

---

## Profile Endpoints

### Get User Profile

**Endpoint**: `GET /api/profile`

**Authentication**: Required (@IsUser)

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Profile retrieved",
  "data": {
    "id": 42,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "mobile": "0712345678",
    "status": "ACTIVE",
    "createdAt": "2026-01-15T08:00:00",
    "addresses": [
      {
        "id": 5,
        "lineOne": "123 Main Street",
        "lineTwo": "Apartment 4B",
        "postalCode": "00100",
        "city": "Colombo",
        "isPrimary": true
      }
    ],
    "orderCount": 5,
    "totalSpent": 500000
  }
}
```

---

### Update User Profile

**Endpoint**: `PUT /api/profile`

**Authentication**: Required (@IsUser)

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "mobile": "0712345678",
  "newPassword": "NewPass@1234",
  "confirmPassword": "NewPass@1234"
}
```

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Profile updated successfully"
}
```

---

### Add Address

**Endpoint**: `POST /api/profile/address`

**Authentication**: Required (@IsUser)

**Request Body**:
```json
{
  "lineOne": "456 Oak Avenue",
  "lineTwo": "Suite 200",
  "postalCode": "00200",
  "cityId": 1,
  "isPrimary": false
}
```

**Response - Success (201)**:
```json
{
  "status": true,
  "message": "Address added successfully",
  "data": {
    "addressId": 6,
    "lineOne": "456 Oak Avenue",
    "postalCode": "00200",
    "cityName": "Colombo"
  }
}
```

---

### Get User Addresses

**Endpoint**: `GET /api/profile/addresses`

**Authentication**: Required (@IsUser)

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Addresses retrieved",
  "data": [
    {
      "id": 5,
      "lineOne": "123 Main Street",
      "lineTwo": "Apartment 4B",
      "postalCode": "00100",
      "cityId": 1,
      "cityName": "Colombo",
      "isPrimary": true
    },
    {
      "id": 6,
      "lineOne": "456 Oak Avenue",
      "lineTwo": "Suite 200",
      "postalCode": "00200",
      "cityId": 2,
      "cityName": "Kandy",
      "isPrimary": false
    }
  ]
}
```

---

### Get User Orders

**Endpoint**: `GET /api/profile/orders`

**Authentication**: Required (@IsUser)

**Query Parameters**:
- `page` (int): Page number (default: 1)
- `status` (string): Filter by status (PENDING, COMPLETED, etc.)

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Orders retrieved",
  "data": [
    {
      "id": 1000,
      "orderNumber": "ORD-2026-05-29-001000",
      "total": 307800,
      "status": "COMPLETED",
      "deliveryType": "WITHIN_CITY",
      "itemCount": 2,
      "createdAt": "2026-05-29T14:30:00",
      "deliveredAt": "2026-05-31T10:00:00",
      "items": [
        {
          "id": 1,
          "productTitle": "Laptop Pro",
          "quantity": 2,
          "price": 150000,
          "rating": null
        }
      ]
    }
  ],
  "pagination": {
    "page": 1,
    "total": 5
  }
}
```

---

## Content Endpoints

### Get Categories

**Endpoint**: `GET /api/content/categories`

**Authentication**: None (public)

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Categories retrieved",
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "icon": "icons/electronics.svg",
      "productCount": 45
    },
    {
      "id": 2,
      "name": "Clothing",
      "icon": "icons/clothing.svg",
      "productCount": 120
    }
  ]
}
```

---

### Get Cities

**Endpoint**: `GET /api/content/cities`

**Authentication**: None (public)

**Response - Success (200)**:
```json
{
  "status": true,
  "message": "Cities retrieved",
  "data": [
    { "id": 1, "name": "Colombo" },
    { "id": 2, "name": "Kandy" },
    { "id": 3, "name": "Galle" },
    { "id": 4, "name": "Jaffna" },
    { "id": 5, "name": "Kurunegala" }
  ]
}
```

---

## Rate Limiting & Best Practices

### Rate Limiting (Not Implemented - Recommended)

```
Standard limits (should be implemented):
- Public endpoints: 100 requests/minute per IP
- Authenticated endpoints: 1000 requests/minute per user
- File upload: 50MB per file, 500MB per day per user
```

### Best Practices

#### 1. Error Handling

```javascript
// Always check response status
try {
  const res = await fetch('/api/users/login', { method: 'POST', body: JSON.stringify(data) });
  const result = await res.json();
  
  if (!result.status) {
    // Handle business error
    console.error('Login failed:', result.message);
  } else {
    // Success
    console.log('User logged in');
  }
} catch (error) {
  // Handle network error
  console.error('Network error:', error);
}
```

#### 2. Pagination

```javascript
// Use pagination for large datasets
async function getAllProducts() {
  let page = 1;
  let hasMore = true;
  let allProducts = [];
  
  while (hasMore) {
    const res = await fetch(`/api/products?page=${page}&size=50`);
    const result = await res.json();
    allProducts = allProducts.concat(result.data);
    hasMore = page < result.pagination.pages;
    page++;
  }
  
  return allProducts;
}
```

#### 3. Caching

```javascript
// Cache product details to reduce API calls
const productCache = {};

async function getProduct(productId) {
  if (productCache[productId]) {
    return productCache[productId];
  }
  
  const res = await fetch(`/api/products/${productId}`);
  const result = await res.json();
  productCache[productId] = result.data;
  return result.data;
}
```

#### 4. Debouncing

```javascript
// Debounce search to prevent excessive API calls
function debounce(func, delay) {
  let timeoutId;
  return function(...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
}

const searchProducts = debounce(async (query) => {
  const res = await fetch(`/api/products?search=${query}`);
  const result = await res.json();
  displayResults(result.data);
}, 300); // Wait 300ms after typing stops

document.getElementById('searchBox').addEventListener('input', (e) => {
  searchProducts(e.target.value);
});
```

#### 5. Request Timeouts

```javascript
// Always set timeout for requests
async function fetchWithTimeout(url, options = {}, timeout = 5000) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);
  
  try {
    const response = await fetch(url, { ...options, signal: controller.signal });
    clearTimeout(timeoutId);
    return response;
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === 'AbortError') {
      throw new Error('Request timeout');
    }
    throw error;
  }
}
```

---

## Testing with cURL

### Complete Login & Add to Cart Flow

```bash
#!/bin/bash

# 1. Login
echo "Logging in..."
curl -X POST http://localhost:8080/aesthetica/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass@1234"
  }'

# 2. Get products
echo "\nFetching products..."
curl http://localhost:8080/aesthetica/api/products \
  -b cookies.txt

# 3. Add to cart (assuming stock ID 101)
echo "\nAdding to cart..."
curl -X POST http://localhost:8080/aesthetica/api/cart \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"stockId": 101, "quantity": 2}'

# 4. View cart
echo "\nViewing cart..."
curl http://localhost:8080/aesthetica/api/cart \
  -b cookies.txt

# 5. Checkout
echo "\nCreating order..."
curl -X POST http://localhost:8080/aesthetica/api/checkout \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "deliveryTypeId": 1,
    "discountCode": "DEFAULT",
    "addressId": 1
  }'

# 6. Logout
echo "\nLogging out..."
curl -X GET http://localhost:8080/aesthetica/api/users/logout \
  -b cookies.txt
```

---

## Conclusion

The Aesthetica API provides comprehensive functionality for an e-commerce platform. All endpoints follow RESTful conventions and return consistent JSON responses. Authentication uses HTTP sessions for security and stateful management.

For questions or issues, refer to the main DOCUMENTATION.md for architectural details or DATABASE_SCHEMA.md for data model specifics.

