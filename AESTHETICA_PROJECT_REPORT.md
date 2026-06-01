# Aesthetica Project Report

**Project Title:** Aesthetica – Modern E-Commerce Platform for Aesthetic Digital Assets  
**Project Type:** Web Application Development  
**Technologies Used:** Java 17, Hibernate 6, Jersey 3, MySQL, HTML5, CSS3, JavaScript, Bootstrap  
**Prepared For:** Web Programming II Project Submission  
**Prepared By:** [Student Name]  
**Registration Number:** [Student Registration Number]  
**Instructor / Supervisor:** [Name]  
**Date:** June 2026

---

## Acknowledgment

I would like to express my sincere gratitude to my instructor, supervisor, and everyone who supported me during the development of the **Aesthetica** project. Their guidance, feedback, and encouragement helped me complete this project successfully.

I also thank the client representatives and users who provided valuable information about the business process, requirements, and expectations. Their input was essential in designing a system that is practical, user-friendly, and suitable for the business environment.

Finally, I would like to thank my family and friends for their support throughout the development and reporting process.

---

## Table of Contents

1. Acknowledgment  
2. Table of Contents  
3. Introduction  
4. Problem Analysis  
5. Project Plan  
   5.1 Project Proposal Content  
6. Research  
   6.1 Business Research  
7. Analysis  
   7.1 Business Process Diagrams  
8. Design  
   8.1 System Design  
   8.2 Database Design  
   8.3 Storyboards (Wireframes)  
9. Implementation  
10. Limitations and Future Enhancements  
11. Project Experience and Learning Points  
12. Conclusion  
13. References  
14. Appendices  

---

## 3. Introduction

**Aesthetica** is a modern e-commerce web application developed for the sale of aesthetic digital products such as curated image packs, wallpapers, and visual content collections. The system provides users with a smooth browsing and purchasing experience through a responsive frontend and a robust Java backend.

The application is built using a layered architecture that separates presentation, business logic, data access, and database layers. This approach improves maintainability, scalability, and modularity. The backend uses **Hibernate ORM** for database interaction and **Jersey REST API** for communication between the frontend and backend.

The project was developed to demonstrate full-stack web development skills, including requirements analysis, system design, database modeling, backend implementation, frontend development, and documentation.

---

## 4. Problem Analysis

### Problem Description

The business of the client faced several operational and customer experience challenges in managing and selling digital aesthetic content. These included:

- Lack of a centralized online platform for browsing and purchasing products.
- Manual handling of product display and order processing.
- Limited user experience for searching, filtering, and selecting products.
- Difficulty in managing carts, checkout, and order records efficiently.
- No unified system for user accounts, profile management, and address handling.
- Payment and checkout flow not being integrated in a structured way.

### Impact of the Problem

Because of these issues, customers could not conveniently explore products or complete purchases in a streamlined manner. The business also faced operational inefficiency, duplicated effort, and reduced scalability.

### Problem Statement

A web-based e-commerce system was required to provide an organized platform for product browsing, cart management, checkout, user authentication, and order processing.

---

## 5. Project Plan

The project plan was developed to guide the analysis, design, implementation, testing, and documentation phases of the Aesthetica system.

### Project Objectives

- Develop an attractive and responsive e-commerce website.
- Allow users to browse products by category and search terms.
- Provide shopping cart and checkout functionality.
- Support user registration, login, profile management, and addresses.
- Store and manage product, cart, order, and user data using Hibernate and MySQL.
- Integrate secure payment handling using PayHere.

### Major Phases

1. Requirement gathering and research  
2. Business process analysis  
3. System and database design  
4. Frontend and backend implementation  
5. Testing and debugging  
6. Final documentation and report preparation  

### Tools and Technologies

- **Frontend:** HTML5, CSS3, JavaScript, Bootstrap  
- **Backend:** Java, Jersey REST API  
- **Persistence:** Hibernate ORM, MySQL  
- **Server:** Apache Tomcat  
- **Build Tool:** Maven  

### Project Proposal Content

#### Features of the System

- User registration and login
- Product browsing and category filtering
- Product search
- Shopping cart management
- Checkout and payment integration
- User profile and address management
- Order processing
- Responsive user interface
- Admin-ready layered architecture

#### Limitations

- The system currently focuses on the core customer shopping flow.
- Advanced analytics and dashboards are not fully implemented.
- Multi-vendor operations are limited compared with a large-scale marketplace.
- Some administrative features may need further development.

#### Future Enhancements

- Advanced recommendation engine
- Wishlist and favorite product features
- Coupon management improvements
- Order tracking with delivery status updates
- Notification system for order progress
- Admin dashboard and reporting module
- Mobile application version

---

## 6. Research

### 6.1 Business Research

To understand the business needs and user expectations, several research methods were considered and/or used.

#### Methods Used

1. **Interviewing**  
   Interviews were used to gather direct input from business stakeholders. This method was chosen because it provides detailed and specific information about business requirements, workflow, and pain points.

2. **Observation**  
   Observation of the existing process helped identify how products, orders, and user interactions were handled. This method was useful for understanding practical business operations.

3. **Questionnaires**  
   Questionnaires were used to collect structured feedback from potential users about what they expected from the system.

4. **Record Searching**  
   Existing business records, product information, and content references were reviewed to understand the structure of the business and the type of data that needed to be managed.

#### Why These Methods Were Chosen

These methods were selected because they provide both qualitative and quantitative insights. Interviews and observation help uncover detailed business workflow issues, while questionnaires help gather user opinions efficiently. Record searching helps verify actual data and business content.

#### Summary of Gathered Information

From the research, the following needs were identified:

- Users want a simple and visually appealing platform for browsing digital aesthetic content.
- Product filtering and search are important for user convenience.
- The checkout process should be smooth and secure.
- User accounts and order history improve trust and repeat purchases.
- The business needs a centralized system to manage products, carts, and orders effectively.

---

## 7. Analysis

### 7.1 Business Process Diagrams

The business process of Aesthetica can be summarized as follows:

1. The customer visits the website.
2. The customer browses products and categories.
3. The customer views product details.
4. The customer adds selected products to the cart.
5. The customer logs in or proceeds through checkout.
6. The customer enters address and delivery details.
7. The system calculates totals and processes the order.
8. The payment gateway completes the transaction.
9. The system stores the order and clears the cart.
10. The customer can later view order history.

#### Example Business Process Flow

```text
Customer → Browse Products → Add to Cart → Checkout → Payment → Order Confirmation
```

---

## 8. Design

### 8.1 System Design

The system follows a layered design:

- **Presentation Layer:** HTML, CSS, JavaScript frontend pages
- **API Layer:** Jersey REST controllers
- **Business Layer:** Service classes containing business rules
- **Data Access Layer:** Hibernate-based entity persistence
- **Database Layer:** MySQL database

#### 8.1.1 Activity Diagram

**Activity Diagram Summary:**

- Start
- Browse products
- Select product
- Add to cart
- View cart
- Enter checkout details
- Select delivery type
- Confirm order
- Process payment
- Save order
- End

#### 8.1.2 Use Case Diagram

**Main Actors:**

- Customer
- Admin
- Seller / Business Staff

**Main Use Cases:**

- Register account
- Login/logout
- Browse products
- Search products
- Add/remove cart items
- Checkout and pay
- View profile
- View order history
- Manage products and content (admin/seller side)

### 8.2 Database Design

The database was designed using Hibernate ORM entities and mapped to a normalized MySQL schema.

#### 8.2.1 ER Diagram

The main entities include:

- User
- Address
- Product
- Stock
- Cart
- Order
- OrderItem
- Category
- Seller
- Status
- City
- DeliveryType
- Discount

These entities are connected through one-to-many and many-to-one relationships.

#### 8.2.2 Database Schema (Generated via Hibernate Mapping)

Key tables include:

- `users`
- `address`
- `product`
- `stock`
- `cart`
- `orders`
- `order_items`
- `category`
- `seller`
- `status`
- `city`
- `delivery_types`
- `discount`
- `product_images`

The schema is normalized and supports user accounts, products, cart items, checkout, and order history.

#### 8.2.3 Assumptions

- Each user may have multiple addresses.
- One address can be marked as primary.
- Products may have multiple stock records.
- Stock quantities are reduced after order processing.
- The order contains multiple order items.
- Payment is handled through an external gateway.

### 8.3 Storyboards (Wireframes)

The storyboard/wireframe design includes the following pages:

- **Home Page:** Featured products, categories, hero section, and navigation.
- **Shop Page:** Product listing with filters and search.
- **Product View Page:** Detailed product images and add-to-cart action.
- **Cart/Checkout Page:** Cart summary, delivery details, and confirmation.
- **Profile Page:** User details, addresses, and order history.
- **Sign In / Sign Up Pages:** Authentication and registration.

---

## 9. Implementation

### 9.1 Brief Description of Implementation

The Aesthetica system was implemented as a full-stack Java web application. The frontend was built using HTML, CSS, and JavaScript, while the backend uses Java, Jersey REST controllers, and Hibernate ORM for database access.

The system was developed to run on Apache Tomcat and connect to a MySQL database. The application follows a modular design where controllers handle HTTP requests, services process business logic, and entities map to database tables.

#### Deployment Environment

- **Application server:** Apache Tomcat  
- **Database server:** MySQL  
- **Development environment:** IntelliJ IDEA / NetBeans compatible  
- **Frontend hosting:** Static pages under `src/main/webapp`  

#### Machines Installed

The project can be installed on multiple machines depending on deployment requirements:

- Development machine for coding and testing
- Server machine for hosting the application
- Client machines for accessing the web application through a browser

#### Training Provided to Users

Basic user training includes:

- How to register and login
- How to search and browse products
- How to add items to cart
- How to proceed to checkout
- How to manage profile and address details

Administrative users may require additional training on product and order handling.

#### User Comments and Feedback

Typical feedback from users includes:

- The website is visually appealing and modern.
- Product browsing is easy and intuitive.
- The checkout process is straightforward.
- The interface is responsive and easy to use on different screen sizes.

### 9.2 User Acceptance Letter

**[Attach the client’s signed user acceptance letter here.]**

If the signed letter is not yet available, include it as an appendix or attachment before final submission.

---

## 10. Limitations and Future Enhancements

### Limitations

- Limited advanced reporting tools
- No mobile app version yet
- Limited automation for administrative operations
- Payment gateway and post-order workflows may need more validation in production

### Future Enhancements

- Add admin dashboard and analytics
- Add wishlist and comparison features
- Add order tracking and notifications
- Improve guest checkout experience
- Add reviews and ratings system
- Add promotional banners and coupon improvements
- Integrate push notifications or email campaigns

---

## 11. Project Experience and Learning Points

While developing this project, I gained practical experience in:

- Full-stack web application development
- Java REST API creation using Jersey
- Hibernate entity mapping and CRUD operations
- Database design and schema normalization
- Session-based authentication and user management
- Frontend design with responsive layouts
- Checkout and order processing logic
- Integrating payment-related workflows
- Writing technical documentation for a software project

This project improved my understanding of how frontend, backend, and database layers work together in a real-world web system.

---

## 12. Conclusion

Aesthetica is a complete e-commerce platform designed to provide a modern and organized shopping experience for aesthetic digital products. The system successfully addresses the major business needs of product presentation, cart handling, checkout, profile management, and order processing.

By using Java, Hibernate, Jersey, and MySQL, the project demonstrates a well-structured layered architecture and good software engineering practices. The system is scalable, maintainable, and suitable for future enhancements.

---

## 13. References

The following sources were used to gather technical and project-related information:

1. Oracle Java Documentation – https://docs.oracle.com/en/java/
2. Hibernate ORM Documentation – https://hibernate.org/orm/documentation/
3. Jersey REST Framework Documentation – https://eclipse-ee4j.github.io/jersey/
4. MySQL Documentation – https://dev.mysql.com/doc/
5. Bootstrap Documentation – https://getbootstrap.com/docs/
6. PayHere Payment Gateway Documentation – https://www.payhere.lk/
7. W3Schools and MDN Web Docs for HTML, CSS, and JavaScript reference
8. Internal project files and documentation:
   - `README.md`
   - `DOCUMENTATION.md`
   - `DATABASE_SCHEMA.md`
   - `API_REFERENCE.md`
   - `SETUP_GUIDE.md`

---

## 14. Appendices

### 14.1 Appendix A – Business Research Documents

#### A.1 Forms of the Company

**[Attach company registration forms or business profile documents here.]**

#### A.2 Questionnaires

**Sample Questionnaire Questions:**

1. What type of digital products do you want to sell online?
2. What problems do you currently face in handling orders?
3. What information do customers usually request?
4. Which features are most important in an online shopping platform?
5. What type of checkout process would be most suitable?

#### A.3 Interview Questions

**Sample Interview Questions:**

1. How do you currently manage product listings?
2. How are customer orders processed?
3. What difficulties do you experience in manual order handling?
4. What payment methods are preferred by your customers?
5. What improvements do you expect from the new system?

---

### 14.2 Appendix B – Minutes of the Meetings

> Minimum 6 meetings should be included. The following format can be repeated and updated with real meeting details.

#### Meeting 1
- **Date and Time:** [Insert date and time]
- **Location:** [Insert location]
- **Resource Person:** [Insert name]
- **Members Present:** [Insert members]
- **Gathered Information:** Initial business overview and project goals.

#### Meeting 2
- **Date and Time:** [Insert date and time]
- **Location:** [Insert location]
- **Resource Person:** [Insert name]
- **Members Present:** [Insert members]
- **Gathered Information:** Product categories, target customers, and design expectations.

#### Meeting 3
- **Date and Time:** [Insert date and time]
- **Location:** [Insert location]
- **Resource Person:** [Insert name]
- **Members Present:** [Insert members]
- **Gathered Information:** Cart and checkout workflow requirements.

#### Meeting 4
- **Date and Time:** [Insert date and time]
- **Location:** [Insert location]
- **Resource Person:** [Insert name]
- **Members Present:** [Insert members]
- **Gathered Information:** User account, address, and order history requirements.

#### Meeting 5
- **Date and Time:** [Insert date and time]
- **Location:** [Insert location]
- **Resource Person:** [Insert name]
- **Members Present:** [Insert members]
- **Gathered Information:** Database structure and implementation planning.

#### Meeting 6
- **Date and Time:** [Insert date and time]
- **Location:** [Insert location]
- **Resource Person:** [Insert name]
- **Members Present:** [Insert members]
- **Gathered Information:** Final feedback, testing, and deployment considerations.

---

### 14.3 Appendix C – User Manual

#### Login / Registration
1. Open the website.
2. Click Sign Up to create a new account.
3. Fill in personal details and submit.
4. Login using your email and password.

#### Browsing Products
1. Go to the home or shop page.
2. Browse categories or search for products.
3. Open a product to view more details.

#### Adding to Cart
1. Select a product quantity.
2. Click Add to Cart.
3. View the cart icon to confirm the item has been added.

#### Checkout
1. Open the cart or checkout page.
2. Enter delivery details.
3. Confirm the order.
4. Complete payment if required.

#### Profile Management
1. Open My Account.
2. Update personal information.
3. Add or edit addresses.
4. View order history.

---

### 14.4 Appendix D – Hibernate CRUD Operations

Below are example Hibernate snippets instead of SQL queries.

#### 14.4.1 Save
```java
Session session = HibernateUtil.getSessionFactory().openSession();
Transaction tx = session.beginTransaction();
session.save(object);
tx.commit();
session.close();
```

#### 14.4.2 Update
```java
Session session = HibernateUtil.getSessionFactory().openSession();
Transaction tx = session.beginTransaction();
session.update(object);
tx.commit();
session.close();
```

#### 14.4.3 Delete
```java
Session session = HibernateUtil.getSessionFactory().openSession();
Transaction tx = session.beginTransaction();
session.delete(object);
tx.commit();
session.close();
```

#### 14.4.4 Load/Get
```java
Session session = HibernateUtil.getSessionFactory().openSession();
Object obj1 = session.get(Class.class, id);
Object obj2 = session.load(Class.class, id);
session.close();
```

#### 14.4.5 Criteria Queries
```java
Session session = HibernateUtil.getSessionFactory().openSession();
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Class> cq = cb.createQuery(Class.class);
Root<Class> root = cq.from(Class.class);
cq.select(root);
List<Class> list = session.createQuery(cq).getResultList();
session.close();
```

#### 14.4.6 Restrictions
```java
Session session = HibernateUtil.getSessionFactory().openSession();
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Class> cq = cb.createQuery(Class.class);
Root<Class> root = cq.from(Class.class);
cq.select(root).where(cb.equal(root.get("property"), value));
List<Class> list = session.createQuery(cq).getResultList();
session.close();
```

---

### 14.5 Appendix E – UI and Reports

**Attach the following screenshots and reports:**

- Home page UI
- Shop page UI
- Product detail page UI
- Cart/checkout page UI
- Sign in/sign up pages
- My account page UI
- Order confirmation screen
- Database schema screenshots
- Sample generated reports

---

## Final Note

This report is prepared as a structured project report draft for the **Aesthetica** system based on the supplied project report template and the available project documentation.

**End of Report**

