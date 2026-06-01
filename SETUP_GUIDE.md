# Aesthetica - Setup & Deployment Guide

## Complete Setup Instructions

This guide walks through setting up Aesthetica from scratch to running.

---

## Prerequisites Checklist

Before starting, ensure you have:

```
□ Java Development Kit (JDK) 17 or higher
□ Maven 3.6 or higher
□ MySQL Server 5.7 or higher
□ Git (optional, for version control)
□ IDE: IntelliJ IDEA, Eclipse, or NetBeans (optional)
□ Text Editor: VS Code (for web files)
□ Postman or cURL (for API testing)
□ 500MB disk space minimum
```

### Verify Installation

```bash
# Check Java
java -version
# Expected: openjdk version "17" or higher

# Check Maven
mvn -v
# Expected: Apache Maven 3.8+

# Check MySQL
mysql --version
# Expected: mysql Ver 8.0+ or 5.7+
```

---

## Step 1: Set Up MySQL Database

### 1.1 Create Database and User

```bash
# Connect to MySQL as root
mysql -u root -p

# Inside MySQL prompt:
CREATE DATABASE aesthetica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'aesthetica_user'@'localhost' IDENTIFIED BY 'mysql2006';

GRANT ALL PRIVILEGES ON aesthetica.* TO 'aesthetica_user'@'localhost';

FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;
SELECT user FROM mysql.user;

EXIT;
```

### 1.2 Verify Connection

```bash
# Test connection as new user
mysql -u aesthetica_user -p aesthetica -e "SELECT 1;"

# When prompted, enter password: mysql2006
# Expected output: 1 (showing connection successful)
```

### 1.3 Initialize Schema and Data

```bash
# Navigate to project
cd /mnt/winterfell/College/Curriculam/WEB2/Aesthetica

# Run migration (optional - Hibernate can auto-create)
mysql -u aesthetica_user -p aesthetica < src/main/resources/db_migration_2026_02_20.sql

# Run seed data
mysql -u aesthetica_user -p aesthetica < src/main/resources/db_seed_data.sql

# Verify tables created
mysql -u aesthetica_user -p aesthetica -e "SHOW TABLES;"

# Expected tables:
# - users, product, stock, cart
# - orders, order_items, address
# - seller, category, city
# - status, delivery_types, discount
```

**What These Scripts Do**:

**db_migration_2026_02_20.sql**:
- Ensures required status values exist
- Sets default values for product dimensions
- Makes seller optional for products
- Removes old verification columns

**db_seed_data.sql**:
- Inserts predefined status values
- Inserts delivery types with pricing
- Inserts cities (Colombo, Kandy, etc.)
- Inserts default discount (0%)

---

## Step 2: Configure Application

### 2.1 Application Properties

Edit: `src/main/resources/app.properties`

```properties
# ===== EMAIL CONFIGURATION =====
# Using Mailtrap for testing (replace with real SMTP for production)
mail.host=smtp.gmail.com
mail.port=587
mail.username=your-email@gmail.com
mail.password=your-app-specific-password

# ===== APP SETTINGS =====
app.mail=noreply@aesthetica.com
app.name=Aesthetica
app.url=http://localhost:8080/aesthetica

# ===== PAYMENT GATEWAY =====
# PayHere merchant credentials (register at payhere.lk)
payhere.merchant.id=1224621
payhere.merchant.secret=MjgxNzA1MDMzMTk3NzczNDYzMzMyODc4MjcxODUyMjkwNDE2Nzgz
```

**Email Setup Options**:

**Option A: Gmail SMTP**
```properties
mail.host=smtp.gmail.com
mail.port=587
mail.username=your-email@gmail.com
mail.password=your-App-Specific-Password  # NOT your regular Gmail password!
# Generate at: myaccount.google.com/apppasswords
```

**Option B: Mailtrap (Testing)**
```properties
mail.host=sandbox.smtp.mailtrap.io
mail.port=2525
mail.username=YOUR_MAILTRAP_USERNAME
mail.password=YOUR_MAILTRAP_PASSWORD
# Get credentials from mailtrap.io dashboard
```

### 2.2 Database Configuration

Edit: `src/main/resources/hibernate.cfg.xml`

```xml
<!-- Primary MySQL Connection -->
<property name="hibernate.connection.url">
  jdbc:mysql://localhost:3306/aesthetica?useSSL=false&allowPublicKeyRetrieval=true
</property>
<property name="hibernate.connection.username">aesthetica_user</property>
<property name="hibernate.connection.password">mysql2006</property>

<!-- Optional: Fallback H2 Configuration (for testing without MySQL) -->
<!-- Uncomment these to use in-memory H2 database
<property name="hibernate.connection.driver_class">org.h2.Driver</property>
<property name="hibernate.dialect">org.hibernate.dialect.H2Dialect</property>
<property name="hibernate.connection.url">jdbc:h2:mem:aesthetica;DB_CLOSE_DELAY=-1</property>
<property name="hibernate.connection.username">sa</property>
<property name="hibernate.connection.password"></property>
<property name="hibernate.hbm2ddl.auto">create-drop</property>
-->
```

**Connection Parameters**:
- `useSSL=false`: Disable SSL (for local development only)
- `allowPublicKeyRetrieval=true`: Allow MySQL 8.0+ connection
- `hbm2ddl.auto=update`: Auto-update schema (safe for development)
- `hbm2ddl.auto=validate`: Only validate (use for production)

---

## Step 3: Build Project

### 3.1 Clean and Compile

```bash
cd /mnt/winterfell/College/Curriculam/WEB2/Aesthetica

# Clean previous builds
mvn clean

# Compile source code
mvn compile

# Expected: BUILD SUCCESS
```

### 3.2 Create WAR File (Optional)

```bash
# Package as WAR for deployment
mvn package

# Creates: target/aesthetica.war
# Can be deployed to any Tomcat server
```

### 3.3 Skip Tests (If Tests Fail)

```bash
# Skip tests during build
mvn clean compile -DskipTests

# Or run specific tests
mvn test
```

---

## Step 4: Run Application

### Option A: Direct Execution (Development)

```bash
# Navigate to project
cd /mnt/winterfell/College/Curriculam/WEB2/Aesthetica

# Run Main.java directly
mvn compile exec:java -Dexec.mainClass="com.aesthetica.Main"

# Expected console output:
# App URL: http://localhost:8080/aesthetica
# (Wait for Tomcat startup messages)
```

### Option B: IDE Execution

**IntelliJ IDEA**:
1. Right-click on `Main.java` file
2. Select "Run 'Main.main()'"
3. View console for startup messages

**Eclipse**:
1. Right-click on project → Run As → Java Application
2. Select `Main` class

**NetBeans**:
1. Right-click project → Run
2. Or F6 with project selected

### Option C: Background Execution

```bash
# Run in background (Linux/Mac)
nohup mvn exec:java -Dexec.mainClass="com.aesthetica.Main" > app.log 2>&1 &

# View logs
tail -f app.log

# Find process
ps aux | grep Main

# Kill process
kill <PID>
```

---

## Step 5: Test Application

### 5.1 Access Web Interface

Navigate to: **http://localhost:8080/aesthetica**

Expected: Home page with categories, featured products

**If page doesn't load**:
- Check MySQL is running: `mysql -u aesthetica_user -p -e "SELECT 1;"`
- Check logs for Hibernate errors
- Verify database tables exist: `mysql -u aesthetica_user -p aesthetica -e "SHOW TABLES;"`

### 5.2 Test User Registration

```bash
# Open: http://localhost:8080/aesthetica/sign_up.html

# Fill form:
- First Name: John
- Last Name: Doe
- Email: john@example.com
- Mobile: 0712345678
- Password: Test@1234
- Confirm: Test@1234

# Click Register

# Expected: "Account created" success or email validation error
```

### 5.3 Test API Endpoints

Using cURL:

```bash
# Register via API
curl -X POST http://localhost:8080/aesthetica/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"Jane",
    "lastName":"Smith",
    "email":"jane@example.com",
    "password":"Test@1234",
    "confirmPassword":"Test@1234",
    "mobile":"0712345679"
  }'

# Expected response:
# {"status":true,"message":"User created successfully","userId":2}
```

Or using Postman:
1. New POST request to `http://localhost:8080/aesthetica/api/users`
2. Headers: `Content-Type: application/json`
3. Body (JSON):
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234",
  "mobile": "0712345679"
}
```

### 5.4 Test Products

```bash
# Get all products
curl http://localhost:8080/aesthetica/api/products

# Get categories
curl http://localhost:8080/aesthetica/api/content/categories

# Get delivery types
curl http://localhost:8080/aesthetica/api/content/delivery-types
```

---

## Step 6: Database Verification

### Check Tables Created

```bash
mysql -u aesthetica_user -p aesthetica

SHOW TABLES;
DESCRIBE users;
DESCRIBE product;
DESCRIBE stock;

# Count records
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as status_count FROM status;
SELECT COUNT(*) as city_count FROM city;
```

### View Initial Data

```sql
-- Show all statuses
SELECT id, value FROM status;

-- Show cities
SELECT id, name FROM city;

-- Show delivery types
SELECT id, name, price FROM delivery_types;

-- Show discounts
SELECT id, coupon_code, value FROM discount;
```

---

## Production Deployment

### Preparation

1. **Config Management**:
   - Store credentials in environment variables, not config files
   - Use separate files for dev/staging/production

2. **Security**:
   - Use `hbm2ddl.auto=validate` (don't auto-update)
   - Disable `hibernate.show_sql`
   - SQLs statements should be logged only for admin review
   - Use HTTPS, not HTTP
   - Hash passwords (currently passwords stored as plaintext)
   - Add rate limiting to prevent brute force attacks

3. **Database**:
   - Run regular backups: `mysqldump -u aesthetica_user -p aesthetic > backup.sql`
   - Monitor disk usage
   - Enable MySQL query logs for debugging

4. **Performance**:
   - Enable second-level caching in Hibernate
   - Use connection pooling (HikariCP)
   - Implement pagination for large result sets
   - Use database indexes (created in migrations)

### Deployment Steps

```bash
# Build production WAR
mvn clean package -DskipTests

# Copy to production Tomcat
cp target/aesthetica.war /opt/tomcat/webapps/

# Tomcat auto-deploys WAR
# Monitor logs
tail -f /opt/tomcat/logs/catalina.out

# Or deploy with Docker
docker build -t aesthetica:1.0 .
docker run -p 8080:8080 aesthetica:1.0
```

### Environment Variables

```bash
# Set in system or Docker
export DB_HOST=mysql.example.com
export DB_USER=aesthetica_user
export DB_PASS=production_password
export DB_NAME=aesthetica_prod
export MAIL_HOST=mail.example.com
export PAYHERE_MERCHANT_ID=xxx
export PAYHERE_MERCHANT_SECRET=yyy
```

---

## Troubleshooting

### Issue: "Connection refused" or MySQL error

**Solution**:
```bash
# Check MySQL running
sudo service mysql status

# Start if stopped
sudo service mysql start

# Check connection
mysql -u aesthetica_user -p -e "SELECT 1;"

# If still fails, verify credentials in hibernate.cfg.xml
```

### Issue: "Port 8080 already in use"

**Solution**:
```bash
# Find process on port 8080
lsof -i :8080
# or
netstat -tulpn | grep 8080

# Kill process
kill -9 <PID>

# Or change port in Main.java
```

### Issue: "Table already exists" error

**Solution**:
```bash
# Hibernate tried to create table twice
# This is usually fine with hbm2ddl.auto=update

# To clear database completely (dev only):
mysql -u aesthetica_user -p aesthetica -e "DROP DATABASE aesthetica; CREATE DATABASE aesthetica;"

# Then re-seed:
mysql -u aesthetica_user -p aesthetica < src/main/resources/db_seed_data.sql
```

### Issue: "No suitable driver found"

**Solution**:
```bash
# Maven didn't download MySQL driver
mvn clean dependency:resolve

# Check if mysql-connector-j in pom.xml
cat pom.xml | grep mysql

# If missing, add to pom.xml:
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.0.0</version>
</dependency>
```

### Issue: "Email sending fails"

**Solution**:
```bash
# Check mail credentials in app.properties
cat src/main/resources/app.properties | grep mail

# Test SMTP connection
telnet smtp.gmail.com 587

# If Gmail:
# 1. Enable 2-factor authentication
# 2. Create App Specific Password
# 3. Use App Password (not regular password) in config
```

### Issue: "Slow queries or high CPU"

**Solution**:
```bash
# Enable Hibernate logging
hibernate.show_sql=true
hibernate.format_sql=true
log4j.rootLogger=DEBUG

# Check database indexes
mysql -u aesthetica_user -p aesthetica -e "SHOW INDEX FROM users;"

# Monitor MySQL
mysqldumpslow /var/log/mysql/slow.log
```

---

## Performance Tuning

### Database Optimization

```sql
-- Analyze and optimize tables
ANALYZE TABLE users;
OPTIMIZE TABLE orders;

-- Check query execution plan
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';

-- Monitor connections
SHOW PROCESSLIST;

-- Monitor database size
SELECT table_name, 
       ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb 
FROM information_schema.tables 
WHERE table_schema = 'aesthetica' 
ORDER BY (data_length + index_length) DESC;
```

### Hibernate Configuration

```xml
<!-- Connection pooling (optional, requires additional dependency) -->
<property name="connection.provider_class">
  org.hibernate.c3p0.internal.C3P0ConnectionProvider
</property>
<property name="c3p0.min_size">5</property>
<property name="c3p0.max_size">20</property>

<!-- Query caching (enable for production) -->
<property name="hibernate.cache.use_second_level_cache">true</property>
<property name="hibernate.cache.provider_class">
  org.hibernate.cache.SingletonEhCacheRegionFactory
</property>

<!-- Batch operations -->
<property name="hibernate.jdbc.batch_size">20</property>
<property name="hibernate.order_inserts">true</property>
<property name="hibernate.order_updates">true</property>
```

---

## Backup & Recovery

### Regular Backups

```bash
# Daily backup
mysqldump -u aesthetica_user -p aesthetica > users/backups/aesthetica_$(date +%Y%m%d).sql

# Scheduled backup (crontab)
# 0 2 * * * mysqldump -u aesthetica_user -ppassword aesthetica > /backups/aesthetica_daily.sql

# Compressed backup
mysqldump -u aesthetica_user -p aesthetica | gzip > aesthetica_backup.sql.gz
```

### Recovery from Backup

```bash
# Restore database
mysql -u aesthetica_user -p aesthetica < aesthetica_backup.sql

# Restore from compressed backup
gunzip < aesthetica_backup.sql.gz | mysql -u aesthetica_user -p aesthetica
```

---

## Monitoring & Maintenance

### Application Health Checks

```bash
# Check server running
curl -s http://localhost:8080/aesthetica | head -20

# Check API responding
curl -s http://localhost:8080/aesthetica/api/content/cities | jq

# Monitor logs
tail -100f /opt/tomcat/logs/catalina.out

# Check Java process
jps -l | grep Main
```

### Database Health Checks

```sql
-- Check database size
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb
FROM information_schema.tables
WHERE table_schema = 'aesthetica';

-- Find large tables
SELECT COUNT(*) as order_count FROM orders;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as product_count FROM product;

-- Check indexes are being used
SHOW INDEX FROM users;
ANALYZE TABLE users;
```

---

## Conclusion

The Aesthetica application is now ready for use. For development, follow Option A to run locally. For production, follow the Production Deployment section.

**Key Takeaways**:
- Always back up database before migrations
- Monitor logs for errors
- Test after each configuration change
- Use environment variables for sensitive data
- Keep dependencies updated for security

