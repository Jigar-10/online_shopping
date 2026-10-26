-- Extracted and consolidated SQL from DBSC Experiments 3–8 PDFs
-- Database: online_shopping
-- Run in MySQL 8.0 using an account with CREATE USER / GRANT privileges if DCL is included.
-- NOTE: CART, CART_ITEM and REVIEW are referenced by Experiments 4–7 but their CREATE TABLE
-- statements are absent from Experiment 3 PDF. Their schemas below are reconstructed from
-- the INSERT statements and references in the PDFs.
-- NOTE: Experiment 7's check_customer_email trigger sets blank email to NULL, but Experiment 3
-- declares CUSTOMER.email NOT NULL. This source inconsistency is preserved/commented below.

CREATE DATABASE IF NOT EXISTS online_shopping;
USE online_shopping;

-- Drop dependent objects/tables first, allowing the script to be rerun.
DROP VIEW IF EXISTS customer_order_view;
DROP VIEW IF EXISTS customer_view;
DROP VIEW IF EXISTS product_view;
DROP VIEW IF EXISTS payment_view;
DROP VIEW IF EXISTS shipping_view;
DROP TABLE IF EXISTS DIGITAL_WALLET;
DROP TABLE IF EXISTS CREDIT_CARD;
DROP TABLE IF EXISTS PAYMENT;
DROP TABLE IF EXISTS CART_ITEM;
DROP TABLE IF EXISTS REVIEW;
DROP TABLE IF EXISTS ORDER_ITEM;
DROP TABLE IF EXISTS ORDERS;
DROP TABLE IF EXISTS CART;
DROP TABLE IF EXISTS PRODUCT;
DROP TABLE IF EXISTS SUPPLIER;
DROP TABLE IF EXISTS CATEGORY;
DROP TABLE IF EXISTS SHIPPING;
DROP TABLE IF EXISTS ADDRESS;
DROP TABLE IF EXISTS CUSTOMER;
DROP TABLE IF EXISTS DDL_DEMO;
DROP TABLE IF EXISTS TRUNCATE_DEMO;

-- EXPERIMENT 3: DDL / table schema
CREATE TABLE CUSTOMER (
  customer_id INT AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(80) NOT NULL,
  phone VARCHAR(10),
  registered_on DATE NOT NULL DEFAULT (CURRENT_DATE),
  gender VARCHAR(10),
  CONSTRAINT pk_customer PRIMARY KEY (customer_id),
  CONSTRAINT uq_customer_email UNIQUE (email)
);

CREATE TABLE ADDRESS (
  address_id INT AUTO_INCREMENT,
  customer_id INT NOT NULL,
  street VARCHAR(100) NOT NULL,
  city VARCHAR(50) NOT NULL,
  state VARCHAR(50) NOT NULL,
  country VARCHAR(50) NOT NULL,
  pincode VARCHAR(10) NOT NULL,
  CONSTRAINT pk_address PRIMARY KEY (address_id),
  CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id)
);

CREATE TABLE SHIPPING (
  shipment_id INT AUTO_INCREMENT,
  address_id INT NOT NULL,
  carrier VARCHAR(50),
  tracking_no VARCHAR(50) UNIQUE,
  status VARCHAR(30) NOT NULL DEFAULT 'Pending',
  CONSTRAINT pk_shipping PRIMARY KEY (shipment_id),
  CONSTRAINT fk_shipping_address FOREIGN KEY (address_id) REFERENCES ADDRESS(address_id)
);

CREATE TABLE CATEGORY (
  category_id INT AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  CONSTRAINT pk_category PRIMARY KEY (category_id)
);

CREATE TABLE SUPPLIER (
  supplier_id INT AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  contact_email VARCHAR(80) UNIQUE,
  phone VARCHAR(15),
  CONSTRAINT pk_supplier PRIMARY KEY (supplier_id)
);

CREATE TABLE PRODUCT (
  product_id INT AUTO_INCREMENT,
  category_id INT NOT NULL,
  supplier_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  stock_qty INT NOT NULL DEFAULT 0,
  price DECIMAL(10,2) NOT NULL,
  CONSTRAINT pk_product PRIMARY KEY (product_id),
  CONSTRAINT chk_product_stock CHECK (stock_qty >= 0),
  CONSTRAINT chk_product_price CHECK (price >= 0),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id),
  CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(supplier_id)
);

CREATE TABLE CART (
  cart_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id)
);

CREATE TABLE CART_ITEM (
  cart_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  PRIMARY KEY (cart_id, product_id),
  CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0),
  CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES CART(cart_id),
  CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);

CREATE TABLE ORDERS (
  order_id INT AUTO_INCREMENT,
  customer_id INT NOT NULL,
  shipment_id INT,
  order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'Pending',
  CONSTRAINT pk_orders PRIMARY KEY (order_id),
  CONSTRAINT chk_order_amount CHECK (total_amount >= 0),
  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
  CONSTRAINT fk_orders_shipment FOREIGN KEY (shipment_id) REFERENCES SHIPPING(shipment_id)
);

CREATE TABLE ORDER_ITEM (
  order_id INT,
  product_id INT,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  CONSTRAINT pk_order_item PRIMARY KEY (order_id, product_id),
  CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
  CONSTRAINT chk_order_item_price CHECK (unit_price >= 0),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
  CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);

CREATE TABLE PAYMENT (
  payment_id INT AUTO_INCREMENT,
  order_id INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  payment_type VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'Pending',
  CONSTRAINT pk_payment PRIMARY KEY (payment_id),
  CONSTRAINT chk_payment_amount CHECK (amount >= 0),
  CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES ORDERS(order_id)
);

CREATE TABLE CREDIT_CARD (
  payment_id INT,
  card_number VARCHAR(19) NOT NULL,
  cvv VARCHAR(4) NOT NULL,
  expiry_date DATE NOT NULL,
  CONSTRAINT pk_credit_card PRIMARY KEY (payment_id),
  CONSTRAINT fk_card_payment FOREIGN KEY (payment_id) REFERENCES PAYMENT(payment_id)
);

CREATE TABLE DIGITAL_WALLET (
  payment_id INT,
  provider VARCHAR(50) NOT NULL,
  wallet_ref VARCHAR(100) NOT NULL,
  CONSTRAINT pk_digital_wallet PRIMARY KEY (payment_id),
  CONSTRAINT fk_wallet_payment FOREIGN KEY (payment_id) REFERENCES PAYMENT(payment_id)
);

-- Reconstructed from Experiment 4 INSERTs and Experiment 7 trigger references.
CREATE TABLE REVIEW (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  product_id INT NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(255),
  CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id),
  CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id)
);

-- Demonstration tables mentioned in Experiment 3.
CREATE TABLE DDL_DEMO (
  demo_id INT PRIMARY KEY,
  demo_name VARCHAR(50),
  demo_value INT
);

-- EXPERIMENT 4: DML / seed data
INSERT INTO CUSTOMER (customer_id, name, email, phone) VALUES
(1, 'Aarav Sharma', 'aarav@gmail.com', '9876500001'),
(2, 'Priya Patel', 'priya@gmail.com', '9876500002'),
(3, 'Rahul Mehta', 'rahul@gmail.com', '9876500003'),
(4, 'Sneha Shah', 'sneha@gmail.com', '9876500004'),
(5, 'Arjun Verma', 'arjun@gmail.com', '9876500005');

INSERT INTO ADDRESS (address_id, customer_id, street, city, state, country, pincode) VALUES
(1, 1, 'MG Road', 'Mumbai', 'Maharashtra', 'India', '400001'),
(2, 2, 'FC Road', 'Pune', 'Maharashtra', 'India', '411004'),
(3, 3, 'Ring Road', 'Surat', 'Gujarat', 'India', '395003'),
(4, 4, 'Park Street', 'Kolkata', 'West Bengal', 'India', '700016'),
(5, 5, 'Brigade Road', 'Bengaluru', 'Karnataka', 'India', '560001');

INSERT INTO SHIPPING (shipment_id, address_id, carrier, tracking_no, status) VALUES
(1, 1, 'BlueDart', 'TRK1001', 'Delivered'),
(2, 2, 'DTDC', 'TRK1002', 'Shipped'),
(3, 3, 'Delhivery', 'TRK1003', 'Pending'),
(4, 4, 'IndiaPost', 'TRK1004', 'Shipped'),
(5, 5, 'BlueDart', 'TRK1005', 'Delivered');

INSERT INTO CATEGORY (category_id, name) VALUES
(1, 'Electronics'), (2, 'Clothing'), (3, 'Books'),
(4, 'Home Appliances'), (5, 'Accessories');

INSERT INTO SUPPLIER (supplier_id, name, contact_email) VALUES
(1, 'Tech Suppliers', 'tech@example.com'),
(2, 'Fashion Traders', 'fashion@example.com'),
(3, 'Book Distributors', 'books@example.com'),
(4, 'Home Essentials', 'home@example.com'),
(5, 'Accessory World', 'accessories@example.com');

INSERT INTO PRODUCT (product_id, category_id, supplier_id, name, description, stock_qty, price) VALUES
(1, 1, 1, 'Laptop', 'Portable computer', 20, 55000.00),
(2, 2, 2, 'T-Shirt', 'Cotton T-shirt', 100, 799.00),
(3, 3, 3, 'SQL Book', 'Database textbook', 50, 499.00),
(4, 4, 4, 'Mixer', 'Kitchen mixer', 15, 2499.00),
(5, 5, 5, 'Headphones', 'Wireless headphones', 30, 1999.00);

INSERT INTO CART (cart_id, customer_id) VALUES (1,1),(2,2),(3,3),(4,4),(5,5);
INSERT INTO CART_ITEM (cart_id, product_id, quantity) VALUES
(1,1,1),(2,2,2),(3,3,1),(4,4,1),(5,5,2);

INSERT INTO REVIEW (review_id, customer_id, product_id, rating, comment) VALUES
(1,1,1,5,'Excellent product'),
(2,2,2,4,'Good quality'),
(3,3,3,5,'Very useful book'),
(4,4,4,3,'Average product'),
(5,5,5,4,'Good sound quality');

INSERT INTO ORDERS (order_id, customer_id, shipment_id, total_amount, status) VALUES
(1,1,1,55000.00,'Delivered'),
(2,2,2,1598.00,'Shipped'),
(3,3,3,499.00,'Pending'),
(4,4,4,2499.00,'Shipped'),
(5,5,5,3998.00,'Delivered');

INSERT INTO ORDER_ITEM (order_id, product_id, quantity, unit_price) VALUES
(1,1,1,55000.00),(2,2,2,799.00),(3,3,1,499.00),
(4,4,1,2499.00),(5,5,2,1999.00);

INSERT INTO PAYMENT (payment_id, order_id, amount, payment_type, status) VALUES
(1,1,55000.00,'Credit Card','Completed'),
(2,2,1598.00,'Credit Card','Completed'),
(3,3,499.00,'Credit Card','Pending'),
(4,4,2499.00,'Credit Card','Completed'),
(5,5,3998.00,'Credit Card','Completed'),
(6,1,100.00,'Digital Wallet','Completed'),
(7,2,50.00,'Digital Wallet','Completed'),
(8,3,20.00,'Digital Wallet','Pending'),
(9,4,75.00,'Digital Wallet','Completed'),
(10,5,100.00,'Digital Wallet','Completed');

INSERT INTO CREDIT_CARD (payment_id, card_number, cvv, expiry_date) VALUES
(1,'4111111111111001','123','2028-12-31'),
(2,'4111111111111002','234','2029-11-30'),
(3,'4111111111111003','345','2028-10-31'),
(4,'4111111111111004','456','2030-09-30'),
(5,'4111111111111005','567','2029-08-31');

INSERT INTO DIGITAL_WALLET (payment_id, provider, wallet_ref) VALUES
(6,'Paytm','WALLET001'),
(7,'PhonePe','WALLET002'),
(8,'Google Pay','WALLET003'),
(9,'Paytm','WALLET004'),
(10,'PhonePe','WALLET005');

-- EXPERIMENT 5: examples of basic/complex queries
-- The original PDF contains many SELECT exercises; representative query patterns:
-- SELECT * FROM CUSTOMER;
-- SELECT name, price FROM PRODUCT WHERE price > 1000 ORDER BY price DESC;
-- SELECT category_id, COUNT(*) AS product_count FROM PRODUCT GROUP BY category_id;
-- SELECT c.name, o.order_id, o.total_amount FROM CUSTOMER c
--   INNER JOIN ORDERS o ON c.customer_id = o.customer_id;
-- SELECT c.customer_id, c.name FROM CUSTOMER c
--   WHERE NOT EXISTS (SELECT 1 FROM ORDERS o WHERE o.customer_id = c.customer_id);

-- EXPERIMENT 6: DCL accounts and least-privilege grants
-- Run this section only as a MySQL administrator. Passwords are copied from the PDF.
CREATE USER IF NOT EXISTS 'customer'@'localhost' IDENTIFIED BY 'Cust@123';
CREATE USER IF NOT EXISTS 'shop_admin'@'localhost' IDENTIFIED BY 'Admin@123';
CREATE USER IF NOT EXISTS 'shipper'@'localhost' IDENTIFIED BY 'Ship@123';
CREATE USER IF NOT EXISTS 'payment_manager'@'localhost' IDENTIFIED BY 'Pay@123';
CREATE USER IF NOT EXISTS 'review_manager'@'localhost' IDENTIFIED BY 'Review@123';
CREATE USER IF NOT EXISTS 'inventory_manager'@'localhost' IDENTIFIED BY 'Inv@123';
CREATE USER IF NOT EXISTS 'order_manager'@'localhost' IDENTIFIED BY 'Order@123';

GRANT SELECT ON online_shopping.product TO 'customer'@'localhost';
GRANT SELECT ON online_shopping.category TO 'customer'@'localhost';
GRANT SELECT, INSERT, UPDATE ON online_shopping.cart TO 'customer'@'localhost';
GRANT SELECT, INSERT, DELETE ON online_shopping.cart_item TO 'customer'@'localhost';
GRANT SELECT, INSERT ON online_shopping.orders TO 'customer'@'localhost';
GRANT SELECT, INSERT ON online_shopping.order_item TO 'customer'@'localhost';
GRANT SELECT, INSERT ON online_shopping.payment TO 'customer'@'localhost';
GRANT SELECT, INSERT ON online_shopping.review TO 'customer'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE ON online_shopping.product TO 'shop_admin'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON online_shopping.category TO 'shop_admin'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON online_shopping.supplier TO 'shop_admin'@'localhost';

GRANT SELECT ON online_shopping.orders TO 'shipper'@'localhost';
GRANT SELECT ON online_shopping.order_item TO 'shipper'@'localhost';
GRANT SELECT ON online_shopping.address TO 'shipper'@'localhost';
GRANT SELECT, UPDATE ON online_shopping.shipping TO 'shipper'@'localhost';

GRANT SELECT, INSERT, UPDATE ON online_shopping.payment TO 'payment_manager'@'localhost';
GRANT SELECT ON online_shopping.orders TO 'payment_manager'@'localhost';

GRANT SELECT, UPDATE, DELETE ON online_shopping.review TO 'review_manager'@'localhost';
GRANT SELECT ON online_shopping.product TO 'review_manager'@'localhost';
GRANT SELECT ON online_shopping.customer TO 'review_manager'@'localhost';

GRANT SELECT, INSERT, UPDATE ON online_shopping.product TO 'inventory_manager'@'localhost';
GRANT SELECT, INSERT, UPDATE ON online_shopping.supplier TO 'inventory_manager'@'localhost';

GRANT SELECT, UPDATE ON online_shopping.orders TO 'order_manager'@'localhost';
GRANT SELECT, UPDATE ON online_shopping.order_item TO 'order_manager'@'localhost';
GRANT SELECT ON online_shopping.customer TO 'order_manager'@'localhost';
GRANT SELECT ON online_shopping.payment TO 'order_manager'@'localhost';

-- EXPERIMENT 7: views
CREATE VIEW customer_view AS
SELECT customer_id, name, email, phone FROM CUSTOMER;

CREATE VIEW product_view AS
SELECT product_id, name, price, stock_qty FROM PRODUCT;

CREATE VIEW customer_order_view AS
SELECT c.customer_id, c.name, o.order_id, o.total_amount, o.status
FROM CUSTOMER c INNER JOIN ORDERS o ON c.customer_id = o.customer_id;

CREATE VIEW payment_view AS
SELECT payment_id, order_id, amount, payment_type, status FROM PAYMENT;

CREATE VIEW shipping_view AS
SELECT shipment_id, address_id, carrier, tracking_no, status FROM SHIPPING;

-- EXPERIMENT 7: triggers
DELIMITER //

CREATE TRIGGER check_product_price_insert
BEFORE INSERT ON PRODUCT FOR EACH ROW
BEGIN
  IF NEW.price < 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Product price cannot be negative';
  END IF;
END//

CREATE TRIGGER check_product_price_update
BEFORE UPDATE ON PRODUCT FOR EACH ROW
BEGIN
  IF NEW.price < 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Product price cannot be negative';
  END IF;
END//

CREATE TRIGGER check_order_quantity
BEFORE INSERT ON ORDER_ITEM FOR EACH ROW
BEGIN
  IF NEW.quantity <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order quantity must be greater than zero';
  END IF;
END//

CREATE TRIGGER check_product_stock
BEFORE INSERT ON ORDER_ITEM FOR EACH ROW
BEGIN
  DECLARE available_stock INT;
  SELECT stock_qty INTO available_stock FROM PRODUCT WHERE product_id = NEW.product_id;
  IF available_stock < NEW.quantity THEN
    SET NEW.quantity = available_stock;
  END IF;
END//

CREATE TRIGGER reduce_product_stock
AFTER INSERT ON ORDER_ITEM FOR EACH ROW
BEGIN
  UPDATE PRODUCT SET stock_qty = stock_qty - NEW.quantity WHERE product_id = NEW.product_id;
END//

CREATE TRIGGER check_customer_email
BEFORE INSERT ON CUSTOMER FOR EACH ROW
BEGIN
  IF NEW.email = '' THEN SET NEW.email = NULL; END IF;
END//

CREATE TRIGGER check_review_rating
BEFORE INSERT ON REVIEW FOR EACH ROW
BEGIN
  IF NEW.rating < 1 OR NEW.rating > 5 THEN SET NEW.rating = 5; END IF;
END//

CREATE TRIGGER check_address_pincode
BEFORE INSERT ON ADDRESS FOR EACH ROW
BEGIN
  IF NEW.pincode NOT REGEXP '^[0-9]{6}$' THEN SET NEW.pincode = '000000'; END IF;
END//

CREATE TRIGGER check_payment_amount_update
BEFORE UPDATE ON PAYMENT FOR EACH ROW
BEGIN
  IF NEW.amount <= 0 THEN SET NEW.amount = 1; END IF;
END//

CREATE TRIGGER check_order_total
BEFORE INSERT ON ORDERS FOR EACH ROW
BEGIN
  IF NEW.total_amount < 0 THEN SET NEW.total_amount = 0; END IF;
END//

DELIMITER ;

-- EXPERIMENT 8: TCL examples (run each block separately to observe effects)
-- COMMIT example:
-- START TRANSACTION;
-- INSERT INTO CUSTOMER (name, email, phone) VALUES ('Test Customer', 'test@example.com', '9000000000');
-- COMMIT;
--
-- ROLLBACK example:
-- START TRANSACTION;
-- UPDATE PRODUCT SET price = price + 100 WHERE product_id = 1;
-- ROLLBACK;
--
-- SAVEPOINT example:
-- START TRANSACTION;
-- UPDATE PRODUCT SET price = price + 50 WHERE product_id = 1;
-- SAVEPOINT before_second_change;
-- UPDATE PRODUCT SET price = price + 25 WHERE product_id = 1;
-- ROLLBACK TO SAVEPOINT before_second_change;
-- COMMIT;

-- Useful verification:
-- SHOW TABLES;
-- SELECT * FROM CUSTOMER;
-- SELECT * FROM PRODUCT;
-- SELECT * FROM ORDERS;
-- SELECT * FROM PAYMENT;
-- SHOW FULL TABLES WHERE Table_type = 'VIEW';
-- SHOW TRIGGERS;
