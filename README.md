# Aesthetica - Modern E-Commerce Platform

## 🌟 Visual Preview

<div align="center">
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-15.png" alt="Homepage Showcase" width="100%" style="border-radius: 8px; margin-bottom: 12px;" />
  <br/>
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-20.png" alt="Product Grid" width="49%" style="border-radius: 8px; display: inline-block;" />
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-24.png" alt="Gallery Slider" width="49%" style="border-radius: 8px; display: inline-block;" />
  <br/>
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-30.png" alt="Categories Overview" width="49%" style="border-radius: 8px; display: inline-block;" />
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-34.png" alt="Authentication" width="49%" style="border-radius: 8px; display: inline-block;" />
  <br/>
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-45.png" alt="Admin Dashboard" width="49%" style="border-radius: 8px; display: inline-block;" />
  <img src="screenshots/Screenshot%20From%202026-06-01%2021-10-58.png" alt="Checkout Flow" width="49%" style="border-radius: 8px; display: inline-block;" />
</div>

<br/>

A full-stack Java e-commerce application built with Hibernate ORM, Jersey REST API, and MySQL database. Aesthetica provides a complete shopping experience with product browsing, cart management, user authentication, and PayHere payment gateway integration.

## 🎯 Quick Overview

**What is Aesthetica?**
- Modern e-commerce web application for browsing and purchasing Aesthetic digital images.
- Fully responsive frontend with Bootstrap CSS framework
- Robust backend API with session-based authentication
- Secure payment processing via PayHere gateway
- Complete admin functionality for inventory and order management

**Key Features**
- 🛍️ Product catalog with categories and search
- 🛒 Shopping cart with session persistence
- 👤 User authentication and profile management
- 💳 Secure checkout with PayHere payment integration
- 🎯 Product recommendations and featured items
- 📱 Fully responsive design for all devices
- 🔒 Session-based authentication system

## 🚀 Quick Start

**Want to get up and running?** → See [SETUP_GUIDE.md](./SETUP_GUIDE.md)

**First time? Start here:**
1. Read [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) (10 min) - Project overview
2. Follow [SETUP_GUIDE.md](./SETUP_GUIDE.md) - Installation steps
3. Check [API_REFERENCE.md](./API_REFERENCE.md) - Available endpoints

## 📚 Documentation Library

Complete documentation is available in the project root:

| Document | Purpose | Best For |
|----------|---------|----------|
| **[DOCUMENTATION.md](./DOCUMENTATION.md)** | Complete architecture, components & design | Understanding the full project |
| **[API_REFERENCE.md](./API_REFERENCE.md)** | REST API endpoints with examples | Building frontend or integrations |
| **[DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)** | Database structure & relationships | Database work & queries |
| **[SETUP_GUIDE.md](./SETUP_GUIDE.md)** | Installation & deployment | Getting started & production setup |
| **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)** | Quick commands & common tasks | Daily development work |

## 🏗️ Architecture at a Glance

```
┌─────────────────────────────────────────────────────┐
│  Frontend Layer (HTML, CSS, JavaScript)             │
│  - Bootstrap responsive design                      │
│  - ES6 async/await API integration                  │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/JSON
┌──────────────────▼──────────────────────────────────┐
│  REST API Layer (Jersey 3)                          │
│  - Authentication & Authorization                   │
│  - 25+ REST endpoints                               │
│  - Session management                               │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│  Business Logic Layer (Services)                    │
│  - User management & authentication                 │
│  - Product & category management                    │
│  - Cart & order processing                          │
│  - Payment gateway integration                      │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│  Data Access Layer (Hibernate ORM)                  │
│  - Entity mapping & relationships                   │
│  - Named queries & repository pattern               │
│  - Transaction management                          │
└──────────────────┬──────────────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────────────┐
│  Database Layer (MySQL 5.7+)                        │
│  - 15 normalized tables                             │
│  - Foreign key relationships                        │
│  - Indexes for performance                          │
└─────────────────────────────────────────────────────┘
```

## 💻 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 17 |
| **Build** | Maven | 3.6+ |
| **ORM** | Hibernate | 6.1.7 |
| **REST API** | Jersey | 3.1.2 |
| **Web Server** | Tomcat | 10.1.7 |
| **Database** | MySQL | 5.7+ |
| **Frontend** | HTML5, CSS3, JavaScript ES6 | - |
| **UI Framework** | Bootstrap | 4/5 |

## 📊 Database Overview

15 tables organized in 3NF normalization:

| Category | Tables |
|----------|--------|
| **User Management** | users, user_roles, user_addresses |
| **Products** | products, categories, product_images |
| **Shopping** | shopping_cart, orders, order_items |
| **Reference Data** | cities, delivery_types, payment_methods |
| **System** | email_templates, audit_logs |

## 🔧 Common Tasks

### Setup & Deployment
```bash
# New installation
→ See SETUP_GUIDE.md → "Installation" section

# Local development
→ See SETUP_GUIDE.md → "Running Locally"

# Production deployment
→ See SETUP_GUIDE.md → "Production Deployment"
```

### Development
```bash
# Test an API endpoint
→ See API_REFERENCE.md (with cURL examples)

# Database queries
→ See DATABASE_SCHEMA.md (with SQL examples)

# Extend functionality
→ See DOCUMENTATION.md → "Components & Services"
```

### Debugging
```bash
# Technical issues
→ See SETUP_GUIDE.md → "Troubleshooting"

# API problems
→ See API_REFERENCE.md → "Error Responses"

# Development tips
→ See QUICK_REFERENCE.md → "Debugging Tips"
```

## 🎓 Learning Paths

Choose based on your role:

**🆕 I'm New to the Project**
1. QUICK_REFERENCE.md (10 min)
2. SETUP_GUIDE.md (follow steps)
3. DOCUMENTATION.md (architecture section)

**💻 Frontend Developer**
1. API_REFERENCE.md (understand endpoints)
2. QUICK_REFERENCE.md (common tasks)
3. DOCUMENTATION.md (component details)

**🗄️ Backend Developer**
1. DOCUMENTATION.md (architecture)
2. DATABASE_SCHEMA.md (schema design)
3. API_REFERENCE.md (endpoint specs)

**📊 Database Admin**
1. DATABASE_SCHEMA.md (complete schema)
2. SETUP_GUIDE.md (setup & maintenance)
3. DOCUMENTATION.md (relationships)

**🚀 DevOps / Deployment**
1. SETUP_GUIDE.md (entire guide)
2. QUICK_REFERENCE.md (quick commands)
3. DOCUMENTATION.md (architecture overview)

## 📝 Key Configuration Files

- `pom.xml` - Maven dependencies and build configuration
- `src/main/resources/app.properties` - Application settings (PayHere credentials, email, etc.)
- `src/main/resources/hibernate.cfg.xml` - Hibernate ORM configuration
- `src/main/webapp/WEB-INF/web.xml` - Servlet and Jersey configuration

## 🔒 Security Features

- ✅ Session-based authentication
- ✅ Password validation and hashing
- ✅ Role-based authorization (User, Admin)
- ✅ CSRF protection ready
- ✅ Input validation on all endpoints
- ✅ Secure payment gateway integration

## 🆘 Getting Help

| Question | Answer |
|----------|--------|
| *How do I install?* | → [SETUP_GUIDE.md](./SETUP_GUIDE.md) |
| *What API endpoints exist?* | → [API_REFERENCE.md](./API_REFERENCE.md) |
| *How does the architecture work?* | → [DOCUMENTATION.md](./DOCUMENTATION.md) |
| *What's the database structure?* | → [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) |
| *What's the quickest way to...?* | → [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) |
| *I got an error, what do I do?* | → [SETUP_GUIDE.md](./SETUP_GUIDE.md#troubleshooting) |

## 📞 Support & Documentation

All comprehensive documentation is included in the project:

- 📖 **50,000+ words** of detailed documentation
- 📊 **200+ code examples** and SQL queries
- 🔗 **Cross-referenced** for easy navigation
- 🎯 **Role-based** learning paths
- 📱 **Markdown format** - readable everywhere

## 📈 Project Statistics

- **Backend**: 10+ Java classes (Controllers, Services, DAOs)
- **Frontend**: 10+ HTML pages with responsive design
- **Database**: 15 tables with normalized schema
- **API**: 25+ REST endpoints
- **Tests**: Included test suite

## 👨‍💻 Development

Want to contribute or extend Aesthetica?

1. Start with [DOCUMENTATION.md](./DOCUMENTATION.md) to understand the architecture
2. Check [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) for entity details
3. Review [API_REFERENCE.md](./API_REFERENCE.md) for endpoint patterns
4. Use [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) for quick lookups

## 📄 License

Created for educational purposes.

## 🎉 Ready to Start?

**→ [View SETUP_GUIDE.md](./SETUP_GUIDE.md) to get started!**

Or jump directly to the documentation you need:
- [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) - 10 minute overview
- [DOCUMENTATION.md](./DOCUMENTATION.md) - Deep dive into architecture
- [API_REFERENCE.md](./API_REFERENCE.md) - API endpoint reference
- [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) - Database structure

---

**Version**: 1.0 | **Last Updated**: May 29, 2026 | **Status**: Production Ready

