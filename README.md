# Online Shopping DBMS Desktop Studio (Java Swing + MySQL 8.0)

A comprehensive, production-grade desktop DBMS management application developed for DBMS college project requirements (covering Experiments 3, 4, 5, 6, 7, and 8).

> 🌐 **Live Web Showcase (GitHub Pages):** [https://jigar-10.github.io/online_shopping/](https://jigar-10.github.io/online_shopping/)
> 
> Explore the live interactive Customer Storefront, Admin Studio (14 tables explorer), and DBMS Experiments 3–8 right in your browser!

---

## 🏛 Architecture

```
Java Swing Desktop UI (FlatLaf Modern Theme)
       │
       ▼
Service Layer (Validation, Business Rules & Transactions)
       │
       ▼
DAO / Repository Layer (PreparedStatements & Error Handling)
       │
       ▼
JDBC (MySQL Connector/J 8.3.0)
       │
       ▼
MySQL 8.0 Database (`online_shopping`)
```

- **Clean Layered Architecture**: Strictly separated UI, Business Logic, and Data Access layers.
- **Dual-Portal Integration**: Seamless 1-click switching between **Admin Studio** (full schema CRUD, views, and system management) and **Customer Storefront** (live shopping catalog, shopping cart, reviews, addresses, and order checkout).
- **Synchronized MySQL Live Data**: Admin edits to products, categories, or prices immediately update the Customer Storefront. Customer checkout transactions immediately update `ORDERS`, `ORDER_ITEM`, `PAYMENT`, reduce product stock via database triggers, and reflect in the Admin Dashboard KPIs.
- **Curated Light Aesthetic**: Clean, modern FlatLaf light palette featuring crisp whites (`#FFFFFF`), subtle slate backgrounds (`#F8FAFC`), gentle borders (`#E2E8F0`), and professional indigo accents (`#2563EB`).
- **Async Execution**: All database queries run in background `SwingWorker` threads to prevent Event Dispatch Thread (EDT) freezes.
- **Modern UI/UX**: Live MySQL connection status indicator badge, responsive layout, search and filtering, formatted currency (`₹`), and descriptive dropdowns.

---

## 🔬 DBMS Experiments Coverage

| Experiment | DBMS Topic | Application Implementation |
|---|---|---|
| **Experiment 3** | **DDL (Data Definition Language)** | Complete relational schema with 14 base tables, `PRIMARY KEY`, `FOREIGN KEY`, `NOT NULL`, `UNIQUE`, `CHECK`, and `DEFAULT` constraints. Includes `DDL_DEMO` table support. |
| **Experiment 4** | **DML (Data Manipulation Language)** | Full Create, Read, Update, and Delete (CRUD) operations for every single base table with parameterized `PreparedStatement` to prevent SQL injection. |
| **Experiment 5** | **DQL (Data Query Language)** | Rich multi-column live search, sorting, filtering, and relational joins across parent-child entities. |
| **Experiment 6** | **DCL (Data Control Language)** | Dynamic connection credentials and support for role-based users (`shop_admin`, `customer`, `shipper`, `payment_manager`, `review_manager`, `inventory_manager`, `order_manager`). |
| **Experiment 7** | **Views & Triggers** | Dedicated Read-Only Views browser for 5 views (`customer_view`, `product_view`, `customer_order_view`, `payment_view`, `shipping_view`). Complete enforcement of integrity triggers (stock deduction, price non-negativity, ratings boundaries). |
| **Experiment 8** | **TCL (Transaction Control Language)** | Multi-table atomic transactions (`setAutoCommit(false)`, `commit()`, `rollback()`) when placing multi-item orders, updating inventory, and logging payments. |

---

## 📋 Database Schema & Supported Tables

The application provides complete CRUD management for all 14 base tables:

1. **`CUSTOMER`**: Customer ID, Name, Email, Phone, Registration Date, Gender.
2. **`ADDRESS`**: Address ID, Customer (FK dropdown), Street, City, State, Country, Pincode.
3. **`SHIPPING`**: Shipment ID, Address (FK dropdown), Carrier, Tracking Number, Status.
4. **`CATEGORY`**: Category ID, Category Name.
5. **`SUPPLIER`**: Supplier ID, Supplier Name, Contact Email, Phone.
6. **`PRODUCT`**: Product ID, Category (FK dropdown), Supplier (FK dropdown), Name, Description, Stock Quantity, Price.
7. **`CART`**: Cart ID, Customer (FK dropdown), Creation Timestamp.
8. **`CART_ITEM`**: Cart ID (Composite PK), Product ID (Composite PK), Quantity.
9. **`ORDERS`**: Order ID, Customer (FK dropdown), Shipment (FK dropdown), Order Date, Total Amount, Status.
10. **`ORDER_ITEM`**: Order ID (Composite PK), Product ID (Composite PK), Quantity, Unit Price.
11. **`PAYMENT`**: Payment ID, Order (FK dropdown), Amount, Payment Date, Payment Method, Status.
12. **`CREDIT_CARD`**: Payment ID (PK/FK), Card Number, CVV, Expiry Date.
13. **`DIGITAL_WALLET`**: Payment ID (PK/FK), Provider (Paytm/PhonePe/GPay), Wallet Reference ID.
14. **`REVIEW`**: Review ID, Customer (FK dropdown), Product (FK dropdown), Rating (1–5 Stars), Comment.

---

## 🛍 Customer Storefront Portal (Light White & Slate Theme)

Click **"🛍 Customer Storefront"** in the top bar or sidebar to switch into the shopper view:

- **Active Customer Switcher**: Dropdown in the top header (`Shopping as: [Customer Name]`) switches the active session across real customers in the `CUSTOMER` table.
- **Product Catalog Grid**:
  - Category pill filter tabs (All, Electronics, Fashion, Home Appliances, etc.).
  - Real-time search bar & price sorting (Low to High, High to Low).
  - Modern white product cards displaying title, category badge, formatted price (`₹`), stock badge (In Stock / Out of Stock), "Quick Add to Cart", and "View Details".
- **Product Detail & Review Modal**:
  - Full product specs, supplier information, and stock status.
  - Live customer reviews list with star ratings (★) and verified comments.
  - Interactive "Write a Review" form submitting real records into the `REVIEW` table.
- **Shopping Cart & Checkout**:
  - Real-time cart synchronization stored in `CART` and `CART_ITEM`.
  - Increment, decrement, or remove items with automatic total calculation.
  - Delivery address selection with an inline **"➕ Add New Address"** dialog updating the `ADDRESS` table.
  - Payment method options: **Credit Card** (card number, CVV, expiry date saved to `CREDIT_CARD`) and **Digital Wallet** (Paytm/PhonePe/GPay saved to `DIGITAL_WALLET`).
  - **Atomic Transactional Checkout (ACID)**: Runs in background SwingWorker using `setAutoCommit(false)` to generate shipment, order, order items, and payment, while triggering MySQL trigger `reduce_product_stock` to decrement inventory.
- **My Orders & Tracking**: View past order history with tracking numbers, status badges, and expandable line item breakdowns.
- **My Saved Addresses**: Manage delivery address book with street, city, state, country, and postal code.
- **1-Click Sync & Return**: Click **"⚙ Admin Studio"** at any time to inspect how the placed order, inventory deduction, and payment records reflect across the 14 DBMS tables.

---

## 🚀 How to Run

### Method 1: Double-Click Batch File (Windows)
Double-click `run.bat` in the project root:
```cmd
run.bat
```

### Method 2: Launch with Executable JAR
```cmd
java -jar target\online-shopping-system-1.0.0.jar
```

### Method 3: Run with Maven
```cmd
.\mvn.cmd compile exec:java
```

### Method 4: Run in VS Code / IntelliJ IDEA / Antigravity
1. Open this folder in your IDE.
2. The project is recognized automatically as a standard Maven project.
3. Run the main class: `app.Main` (`src/main/java/app/Main.java`).

---

## ⚙ Database Configuration

Connection configuration is stored in `db.properties` in the project root or configured directly within the GUI:

### Default Settings
```properties
db.host=localhost
db.port=3306
db.name=online_shopping
db.username=root
db.password=YOUR_LOCAL_MYSQL_PASSWORD
```

### Graphical Connection Settings
1. Click the **Connection Status Pill** in the top-right header or navigate to **Connection Config** in the sidebar.
2. Enter your MySQL Server Host, Port, Username, and Password.
3. Click **Test Connection** to verify without restarting the application.
4. Click **Save & Connect** to save your credentials to `db.properties` and refresh all screens live.
5. You can also click **Run / Verify 'online_shopping_full_database.sql'** to automatically create the database, tables, views, triggers, and seed data directly from the desktop UI!

---

## 🧪 Running Automated Tests

Run the JUnit 5 test suite using the included Maven wrapper:
```cmd
.\mvn.cmd test
```

Tests verify:
- Business validation rules (email regex, phone 10 digits, pincode 6 digits, price non-negativity, credit card numbers, ratings)
- Database configuration properties masking and JDBC URL formatting
- Centralized `DatabaseConnection` status classification

---

## 📁 Project Structure

```
dbms ciap/
├── online_shopping_full_database.sql   # Complete database schema, seed data, views, triggers
├── pom.xml                             # Maven build configuration
├── run.bat                             # Windows quick launcher
├── mvn.cmd / mvnw.cmd                  # Bundled Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── app/
│   │   │   │   └── Main.java                      # Application entry point
│   │   │   ├── config/
│   │   │   │   ├── DatabaseConfig.java            # db.properties loader & saver
│   │   │   │   ├── DatabaseConnection.java        # Centralized JDBC connection manager
│   │   │   │   └── DatabaseInitializer.java       # SQL script runner & schema inspector
│   │   │   ├── model/                             # Entity models (Customer, Product, Order, etc.)
│   │   │   ├── dao/                               # DAO layer with PreparedStatements & transactions
│   │   │   ├── service/                           # Business logic, validations, & transactions
│   │   │   ├── ui/
│   │   │   │   ├── MainFrame.java                 # Responsive main window & screen coordinator
│   │   │   │   ├── SidebarPanel.java              # Navigation menu
│   │   │   │   ├── TopBarPanel.java               # Live status bar, theme switch, & breadcrumbs
│   │   │   │   ├── DashboardPanel.java            # Live KPI statistics & recent orders
│   │   │   │   ├── GenericTablePanel.java         # Reusable CRUD table screen with search
│   │   │   │   ├── ViewsPanel.java                # Experiment 7 Read-only views browser
│   │   │   │   ├── dialog/                        # Create/Edit dialog forms for all entities
│   │   │   │   └── util/                          # FlatLaf themes, colors, & notification helpers
│   │   │   └── exception/                         # DatabaseException & ValidationException
│   │   └── resources/
│   │       ├── db.properties.example              # Configuration template
│   │       └── logback.xml                        # Logging configuration
│   └── test/
│       └── java/                                  # JUnit 5 test cases
```
