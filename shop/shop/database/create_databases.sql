-- Create Database for both services
CREATE DATABASE shop_db;

-- Connect to shop_db
\c shop_db;


-- PRODUCT SERVICE TABLES
-- Categories Table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Products Table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    quantity_stock INTEGER NOT NULL CHECK (quantity_stock >= 0),
    id_category BIGINT NOT NULL,
    CONSTRAINT fk_category FOREIGN KEY (id_category) REFERENCES categories(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_products_category ON products(id_category);
CREATE INDEX idx_products_name ON products(name);


-- ORDER SERVICE TABLES
-- Orders Table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) NOT NULL CHECK (statut IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0)
);

-- Order Lines Table
CREATE TABLE order_lines (
    id BIGSERIAL PRIMARY KEY,
    id_order BIGINT NOT NULL,
    id_product BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    CONSTRAINT fk_order FOREIGN KEY (id_order) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY (id_product) REFERENCES products(id)
);

-- Create indexes for better performance
CREATE INDEX idx_orders_date ON orders(date);
CREATE INDEX idx_orders_statut ON orders(statut);
CREATE INDEX idx_order_lines_order ON order_lines(id_order);
CREATE INDEX idx_order_lines_product ON order_lines(id_product);


-- Sample Data for Product Service
-- Insert sample categories
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Apparel and fashion items'),
('Books', 'Physical and digital books'),
('Home & Garden', 'Home improvement and garden supplies');

-- Insert sample products
INSERT INTO products (name, description, price, quantity_stock, id_category) VALUES
('Laptop Dell XPS 15', 'High-performance laptop with 16GB RAM', 1299.99, 50, 1),
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 200, 1),
('T-Shirt Cotton', 'Comfortable cotton t-shirt available in multiple colors', 19.99, 150, 2),
('Jeans Blue Denim', 'Classic blue denim jeans', 49.99, 100, 2),
('Spring Boot in Action', 'Comprehensive guide to Spring Boot development', 39.99, 75, 3),
('Garden Hose 50ft', 'Durable garden hose with spray nozzle', 24.99, 80, 4);


-- Sample Data for Order Service
-- Insert sample orders
INSERT INTO orders (date, statut, total_amount) VALUES
(CURRENT_TIMESTAMP, 'DELIVERED', 1329.98),
(CURRENT_TIMESTAMP, 'PROCESSING', 69.98),
(CURRENT_TIMESTAMP, 'CONFIRMED', 39.99);

-- Insert sample order lines
INSERT INTO order_lines (id_order, id_product, quantity, unit_price) VALUES
(1, 1, 1, 1299.99),
(1, 2, 1, 29.99),
(2, 3, 2, 19.99),
(2, 2, 1, 29.99),
(3, 5, 1, 39.99);