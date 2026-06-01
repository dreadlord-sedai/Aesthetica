# Aesthetica - Quick Start & Developer Reference

## Quick Navigation

### Documentation Files

| Document | Purpose | Key Sections |
|----------|---------|--------------|
| **DOCUMENTATION.md** | Complete project overview | Architecture, components, entity relationships, data flow |
| **DATABASE_SCHEMA.md** | Database design reference | Table definitions, relationships, indexes, migrations |
| **SETUP_GUIDE.md** | Installation & deployment | Prerequisites, MySQL setup, build, run, troubleshooting |
| **API_REFERENCE.md** | REST API specification | Endpoints, request/response formats, examples |
| **This File** | Quick reference | Common tasks, key files, shortcuts |

---

## Quick Start (5 minutes)

### 1. Prerequisites Check
```bash
java -version          # Should be 17+
mvn -v                # Should be 3.6+
mysql -u root -p      # Enter password to test connection
```

### 2. Database Setup
```bash
mysql -u root -p
CREATE DATABASE aesthetica;
CREATE USER 'aesthetica_user'@'localhost' IDENTIFIED BY 'mysql2006';
GRANT ALL PRIVILEGES ON aesthetica.* TO 'aesthetica_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. Run Application
```bash
cd /mnt/winterfell/College/Curriculam/WEB2/Aesthetica
mvn clean compile exec:java -Dexec.mainClass="com.aesthetica.Main"

# App available at: http://localhost:8080/aesthetica
```

### 4. Test It
```bash
# Register: http://localhost:8080/aesthetica/sign_up.html
# Login: http://localhost:8080/aesthetica/sign_in.html
# Browse products: http://localhost:8080/aesthetica/shop.html
```

---

## Key Technologies at a Glance

```
┌─────────────────────────────────────────┐
│         Frontend (Browser)              │
│  HTML5 + CSS3 + JavaScript (ES6)        │
│  Bootstrap Framework                    │
└─────────────────┬───────────────────────┘
                  │ JSON over HTTP
┌─────────────────▼───────────────────────┐
│     Jersey REST API Layer (3.1.2)       │
│  @Path, @GET, @POST, @PUT, @DELETE      │
└─────────────────┬───────────────────────┘
                  │ Service calls
┌─────────────────▼───────────────────────┐
│    Business Logic Services              │
│  UserService, ProductService, etc.      │
└─────────────────┬───────────────────────┘
                  │ Hibernate ORM
┌─────────────────▼───────────────────────┐
│  Hibernate 6.1.7 (Entity Mapping)       │
│  JPA Annotations (@Entity, @ManyToOne)  │
└─────────────────┬───────────────────────┘
                  │ JDBC
┌─────────────────▼───────────────────────┐
│      MySQL Database (5.7+)              │
│  15 Tables, Normalized Schema, Indexes  │
└─────────────────────────────────────────┘
```

---

## Project Structure Shortcuts

```
Aesthetica/
├── src/main/java/com/aesthetica/
│   ├── Main.java ..................... Startup point
│   ├── entity/ ....................... Database entities (User, Product, Order, etc.)
│   ├── service/ ...................... Business logic (UserService, CartService, etc.)
│   ├── controller/api/ ............... REST endpoints
│   ├── dto/ .......................... Data transfer objects
│   ├── middleware/ ................... Authentication filters
│   └── util/ ......................... Utilities (HibernateUtil, Env)
├── src/main/resources/
│   ├── hibernate.cfg.xml ............ Database connection config
│   ├── app.properties ............... App config & credentials
│   └── db_*.sql ..................... Database scripts
├── src/main/webapp/
│   ├── index.html ................... Home page
│   ├── shop.html .................... Product listing
│   ├── checkout.html ................ Checkout page
│   └── assets/ ...................... CSS, JS, images
├── pom.xml ........................... Maven dependencies
└── target/
    └── aesthetica.war ............... Deployable package
```

---

## Common Development Tasks

### Add New Entity

1. **Create Entity Class**
```java
// src/main/java/com/aesthetica/entity/NewEntity.java
@Entity
@Table(name = "new_entity")
public class NewEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    // Getters/Setters...
}
```

2. **Register in hibernate.cfg.xml**
```xml
<mapping class="com.aesthetica.entity.NewEntity"/>
```

3. **Create DTO** (if needed for API)
```java
public class NewEntityDTO {
    private int id;
    private String name;
    // Getters/Setters...
}
```

### Add New API Endpoint

1. **Create Controller Method**
```java
@Path("/new-endpoint")
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response newEndpoint(String jsonData) {
    NewDTO dto = AppUtil.GSON.fromJson(jsonData, NewDTO.class);
    String response = new NewService().doSomething(dto);
    return Response.ok().entity(response).build();
}
```

2. **Create Service**
```java
public class NewService {
    public String doSomething(NewDTO dto) {
        // Business logic here
        Session session = HibernateUtil.getSessionFactory().openSession();
        // ... interact with database
        session.close();
        return "{\"status\": true}";
    }
}
```

3. **Test with cURL**
```bash
curl -X POST http://localhost:8080/aesthetica/api/new-endpoint \
  -H "Content-Type: application/json" \
  -d '{"param": "value"}'
```

### Protect Endpoint with Authentication

Add `@IsUser` annotation:
```java
@GET
@IsUser  // Requires login
@Path("/protected")
public Response protectedEndpoint(@Context HttpServletRequest request) {
    // Code here
}
```

### Query Database

```java
Session session = HibernateUtil.getSessionFactory().openSession();

// Named Query
User user = session.createNamedQuery("User.getByEmail", User.class)
    .setParameter("email", "test@example.com")
    .getSingleResultOrNull();

// HQL Query
List<Product> products = session.createQuery(
    "FROM Product p WHERE p.category.id = :catId", 
    Product.class
).setParameter("catId", 5).getResultList();

// Save/Update
session.beginTransaction();
session.persist(newEntity);  // Insert
session.merge(updatedEntity); // Update
session.getTransaction().commit();

session.close();
```

---

## Validation Rules

### Password Requirements
```
Minimum 8 characters
At least 1 UPPERCASE letter
At least 1 lowercase letter
At least 1 digit (0-9)
At least 1 special character (!@#$%^&*)
```

**Regex**: `^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$`

### Email Format
```
Standard email validation
Must be unique in database
```

### Mobile Number (Sri Lanka)
```
Exactly 10 digits
Format: 07XXXXXXXX or 06XXXXXXXX
```

---

## Database Maintenance Commands

### Backup Database
```bash
mysqldump -u aesthetica_user -p aesthetica > backup_$(date +%Y%m%d).sql
```

### Restore Database
```bash
mysql -u aesthetica_user -p aesthetica < backup_20260529.sql
```

### View Database Stats
```sql
SELECT table_name, ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb
FROM information_schema.tables
WHERE table_schema = 'aesthetica'
ORDER BY (data_length + index_length) DESC;
```

### Optimize Tables
```sql
OPTIMIZE TABLE users, product, orders;
ANALYZE TABLE users, product, orders;
```

---

## Common API Testing Endpoints

### Add User (Registration)
```bash
curl -X POST http://localhost:8080/aesthetica/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "email":"john@example.com",
    "password":"Pass@1234",
    "confirmPassword":"Pass@1234",
    "mobile":"0712345678"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/aesthetica/api/users/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"email":"john@example.com","password":"Pass@1234"}'
```

### Get Products
```bash
curl "http://localhost:8080/aesthetica/api/products?page=1&size=10"
```

### Add to Cart (Requires Login)
```bash
curl -X POST http://localhost:8080/aesthetica/api/cart \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"stockId":1,"quantity":2}'
```

### View Cart (Requires Login)
```bash
curl http://localhost:8080/aesthetica/api/cart -b cookies.txt
```

### Get Categories
```bash
curl http://localhost:8080/aesthetica/api/content/categories
```

---

## Important Entity Relationships

### Key One-to-Many
```
User (1) ----------→ (M) Address
User (1) ----------→ (M) Cart
User (1) ----------→ (M) Order
Product (1) -------→ (M) Stock
Product (1) -------→ (M) OrderItem
Category (1) ------→ (M) Product
Order (1) ---------→ (M) OrderItem
Seller (1) --------→ (M) Product
```

### Key Many-to-One
```
Cart (M) ←---------- (1) Stock
Order (M) ←---------- (1) DeliveryType
Product (M) ←---------- (1) Category
Product (M) ←---------- (1) Seller ← (1) User
```

---

## Debugging Tips

### Enable Hibernate SQL Logging

In `hibernate.cfg.xml`:
```xml
<property name="hibernate.show_sql">true</property>
<property name="hibernate.format_sql">true</property>
```

### View Application Logs
```bash
# If running with nohup
tail -f app.log

# If running in IDE, check console output
```

### Check Database Connection
```bash
mysql -u aesthetica_user -p -e "SELECT 1;"

# If fails, check:
# 1. MySQL service running
# 2. Credentials correct in hibernate.cfg.xml
# 3. User has privileges: GRANT ALL ON aesthetica.* TO 'aesthetica_user'@'localhost'
```

### Check API Availability
```bash
curl -I http://localhost:8080/aesthetica/

# Should return 200 OK

# Test specific endpoint
curl http://localhost:8080/aesthetica/api/content/cities | jq
```

### Verify Entities Mapped
```bash
# Check console output for entity mapping lines:
# INFO: HHH000484: Entity mapped: com.aesthetica.entity.User
# ... (one for each entity)
```

---

## Build & Deployment Commands

### Development Build
```bash
mvn clean compile -DskipTests
```

### Production Build
```bash
mvn clean compile -DskipTests
mvn package -DskipTests
```

### Run Tests
```bash
mvn test
```

### View Dependencies
```bash
mvn dependency:tree
```

### Update Dependencies
```bash
mvn versions:display-dependency-updates
mvn versions:use-latest-releases
```

---

## Files to Remember

### Critical Configuration Files
```
src/main/resources/hibernate.cfg.xml    ← Database connection
src/main/resources/app.properties       ← App settings & credentials
pom.xml                                 ← Dependencies
src/main/webapp/WEB-INF/web.xml         ← Servlet filters
```

### Entry Point
```
src/main/java/com/aesthetica/Main.java  ← Application startup
```

### Core Business Logic
```
src/main/java/com/aesthetica/service/   ← Service layer
```

### Database Definitions
```
src/main/java/com/aesthetica/entity/    ← Entity classes
src/main/resources/db_seed_data.sql     ← Initial data
src/main/resources/db_migration_*.sql   ← Schema migrations
```

---

## Performance Optimization Tips

1. **Use Lazy Loading**: Set `fetch = FetchType.LAZY` on relationships
2. **Index Frequently Queried Columns**: Email, user_id, status_id
3. **Pagination**: Never fetch all records, use page/size
4. **Connection Pooling**: Consider HikariCP for production
5. **Caching**: Enable Hibernate second-level cache for read-heavy queries
6. **Query Optimization**: Use named queries, SELEC only needed fields
7. **Database Indexes**: Check migration scripts for indexes

---

## Next Steps for Enhancement

### Recommended Improvements
1. [ ] Add input rate limiting
2. [ ] Implement password hashing (currently plaintext)
3. [ ] Add email verification for new accounts
4. [ ] Implement seller product approval workflow
5. [ ] Add product reviews/ratings system
6. [ ] Implement refund/return management
7. [ ] Add admin dashboard
8. [ ] Implement search indexing (Elasticsearch)
9. [ ] Add file upload validation and security
10. [ ] Implement transaction logging for audits

### Security Enhancements
- Implement CSRF tokens
- Add rate limiting per IP
- Use HTTPS with valid SSL certificate
- Implement password hashing (bcrypt/scrypt)
- Add input sanitization for all fields
- Use prepared statements (Hibernate does this automatically)
- Implement audit logging
- Add API keys for external integrations

---

## Support & Documentation

### When You Get Stuck

1. **Check Console Output**: Look for error messages and stack traces
2. **Review Logs**: `tail -f app.log`
3. **Verify Configuration**: Check hibernate.cfg.xml and app.properties
4. **Check Database**: `mysql -u aesthetica_user -p aesthetica -e "SHOW TABLES;"`
5. **Test API**: Use cURL or Postman
6. **Read Documentation**:
   - DOCUMENTATION.md - Full overview
   - DATABASE_SCHEMA.md - Database design
   - API_REFERENCE.md - Endpoint specifications
   - SETUP_GUIDE.md - Installation help

### Generate Fresh Database
```bash
# Drop and recreate
mysql -u root -p -e "DROP DATABASE aesthetica; CREATE DATABASE aesthetica;"
mysql -u aesthetica_user -p aesthetica < src/main/resources/db_seed_data.sql

# Or let Hibernate auto-create (hbm2ddl.auto=update)
```

---

## Project Statistics

### Code Metrics
```
Total Java Files: 30+
Total Entity Classes: 13
Total Service Classes: 8
Total API Endpoints: 25+
Total HTML Pages: 8
Total JavaScript Files: 8+
Database Tables: 15
Lines of Java Code: 3000+
```

### Technologies Used
```
Java 17, Maven, Tomcat 10, Jersey 3, Hibernate 6
MySQL 5.7+, HTML5, CSS3, JavaScript ES6
Bootstrap 5, GSON, Jakarta Mail, PayHere API
```

### Project Size
```
Base Code: ~5MB
Compiled JAR: ~25MB
WAR Package: ~8MB (deployable)
```

---

## Contact & Attribution

**Project Name**: Aesthetica E-commerce Platform
**Version**: 1.0
**Last Updated**: May 29, 2026
**Built For**: WEB2 Curriculum - College Web Development

---

## Quick Command Reference

```bash
# Build
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="com.aesthetica.Main"

# Package
mvn package -DskipTests

# Database backup
mysqldump -u aesthetica_user -p aesthetica > backup.sql

# Database restore
mysql -u aesthetica_user -p aesthetica < backup.sql

# Database check
mysql -u aesthetica_user -p aesthetica -e "SELECT COUNT(*) FROM users;"

# Test API
curl http://localhost:8080/aesthetica/api/products

# View logs
tail -f app.log

# Kill process on port 8080
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

---

**Happy Coding! 🚀**

