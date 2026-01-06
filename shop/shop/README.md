# Online Shop - Technical Documentation

**Technologies:** Java 17, Spring Boot 3.2.5, PostgreSQL 15, Maven
**Services:** Product Service (port 8081) and Order Service (port 8082)

---

## 1. Endpoint Definitions

### 1.1 Product Service (http://localhost:8081)

#### Endpoints - Categories

| Method | URL                      | Description                    | Parameters                       | Return Codes         |
|--------|--------------------------|--------------------------------|----------------------------------|----------------------|
| POST   | /api/categories          | Create a new category          | Body: CategoryDTO                | 201 Created          |
| GET    | /api/categories/{id}     | Retrieve a category by ID      | Path: id (Long)                  | 200 OK, 404 Not Found|
| GET    | /api/categories          | Retrieve all categories        | None                             | 200 OK               |
| PUT    | /api/categories/{id}     | Update a category              | Path: id, Body: CategoryDTO      | 200 OK, 404 Not Found|
| DELETE | /api/categories/{id}     | Delete a category              | Path: id (Long)                  | 204 No Content       |

**Request example - Create a category:**
```http
POST http://localhost:8081/api/categories
Content-Type: application/json

{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

#### Endpoints - Products

| Method | URL                             | Description                      | Parameters                       | Return Codes         |
|--------|---------------------------------|----------------------------------|----------------------------------|----------------------|
| POST   | /api/products                   | Create a new product             | Body: ProductDTO                 | 201 Created          |
| GET    | /api/products/{id}              | Retrieve a product by ID         | Path: id (Long)                  | 200 OK, 404 Not Found|
| GET    | /api/products                   | Retrieve all products            | None                             | 200 OK               |
| GET    | /api/products/category/{categoryId} | Retrieve products by category | Path: categoryId (Long)          | 200 OK, 404 Not Found|
| PUT    | /api/products/{id}              | Update a product                 | Path: id, Body: ProductDTO       | 200 OK, 404 Not Found|
| DELETE | /api/products/{id}              | Delete a product                 | Path: id (Long)                  | 204 No Content       |
| POST   | /api/products/decrease-stock    | Decrease product stock           | Body: StockUpdateRequest         | 200 OK, 400 Bad Request |

**Request example - Create a product:**
```http
POST http://localhost:8081/api/products
Content-Type: application/json

{
  "name": "Laptop Dell XPS 15",
  "description": "High-performance laptop with 16GB RAM",
  "price": 1299.99,
  "quantityStock": 50,
  "categoryId": 1
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Laptop Dell XPS 15",
  "description": "High-performance laptop with 16GB RAM",
  "price": 1299.99,
  "quantityStock": 50,
  "categoryId": 1,
  "categoryName": "Electronics"
}
```

**Request example - Decrease stock:**
```http
POST http://localhost:8081/api/products/decrease-stock
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

**Response:**
- **200 OK** with `true` if stock was successfully decreased
- **400 Bad Request** with `false` if insufficient stock

### 1.2 Order Service (http://localhost:8082)

#### Endpoints - Orders

| Method | URL                         | Description                      | Parameters                       | Return Codes         |
|--------|-----------------------------|----------------------------------|----------------------------------|----------------------|
| POST   | /api/orders                 | Create a new order               | Body: CreateOrderRequest         | 201 Created          |
| GET    | /api/orders/{id}            | Retrieve an order by ID          | Path: id (Long)                  | 200 OK, 404 Not Found|
| GET    | /api/orders                 | Retrieve all orders              | None                             | 200 OK               |
| PUT    | /api/orders/{id}/status     | Update order status              | Path: id, Query: status (String) | 200 OK, 404 Not Found|
| DELETE | /api/orders/{id}            | Delete an order                  | Path: id (Long)                  | 204 No Content       |

**Possible order statuses:** PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

**Request example - Create an order:**
```http
POST http://localhost:8082/api/orders
Content-Type: application/json

{
  "items": [
    {
      "idProduct": 1,
      "quantity": 2
    },
    {
      "idProduct": 2,
      "quantity": 1
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "date": "2025-12-11T10:30:00",
  "statut": "CONFIRMED",
  "totalAmount": 2849.97,
  "orderLines": [
    {
      "id": 1,
      "orderId": 1,
      "idProduct": 1,
      "quantity": 2,
      "unitPrice": 1299.99,
      "lineTotal": 2599.98
    },
    {
      "id": 2,
      "orderId": 1,
      "idProduct": 2,
      "quantity": 1,
      "unitPrice": 249.99,
      "lineTotal": 249.99
    }
  ]
}
```

**Request example - Update status:**
```http
PUT http://localhost:8082/api/orders/1/status?status=SHIPPED
```

**Response (200 OK):**
```json
{
  "id": 1,
  "date": "2025-12-11T10:30:00",
  "statut": "SHIPPED",
  "totalAmount": 2849.97,
  "orderLines": [...]
}
```

---

## 2. Implementation Logic Explanation

### 2.1 Application Logic

Our e-commerce application is based on a **microservices architecture** with two independent services that communicate with each other:

#### Product Service - Product and Category Management

**Role:** Manage the product catalog and maintain inventory (stock).

**Implemented rules:**

1. **Category name uniqueness**: When creating or updating a category, the system verifies that no other category already has that name to avoid duplicates.

2. **Product validation**:
   - Price must be strictly greater than 0
   - Stock can never be negative
   - The category associated with the product must exist in the database

3. **Atomic stock management**: Stock decrease uses a transaction (`@Transactional`) to guarantee data consistency. The SQL query verifies that sufficient stock is available before decreasing it: `WHERE quantity_stock >= :quantity`.

#### Order Service - Order Management

**Role:** Create and manage customer orders by communicating with Product Service to validate and update stock.

**Order creation process:**

1. **Product validation**: For each product in the order, the service verifies its existence by calling Product Service via an HTTP GET request.

2. **Available stock verification**: Before creating the order, the service ensures that each product has sufficient stock available.

3. **Stock decrease**: The service calls the Product Service endpoint `/api/products/decrease-stock` for each product. If the decrease fails (insufficient stock), the entire transaction is rolled back thanks to `@Transactional`.

4. **Total amount calculation**: The service calculates the order's total amount by multiplying the unit price of each product by the ordered quantity.

5. **Order creation**: If all previous steps succeed, the order is saved with status `CONFIRMED` and all order lines are saved.

**Simplified code illustrating the logic:**
```java
@Transactional
public OrderDTO createOrder(CreateOrderRequest request) {
    Order order = new Order();
    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderLineRequest item : request.getItems()) {
        // 1. Retrieve product information
        ProductDTO product = productServiceClient.getProduct(item.getIdProduct());
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        // 2. Verify available stock
        if (product.getQuantityStock() < item.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        // 3. Decrease stock (call to Product Service)
        boolean success = productServiceClient.decreaseStock(
            item.getIdProduct(),
            item.getQuantity()
        );
        if (!success) {
            throw new RuntimeException("Failed to decrease stock");
        }

        // 4. Create order line with current price
        OrderLine orderLine = new OrderLine();
        orderLine.setIdProduct(item.getIdProduct());
        orderLine.setQuantity(item.getQuantity());
        orderLine.setUnitPrice(product.getPrice());
        order.addOrderLine(orderLine);

        // 5. Calculate total
        BigDecimal lineTotal = product.getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity()));
        totalAmount = totalAmount.add(lineTotal);
    }

    order.setTotalAmount(totalAmount);
    order.setStatut(OrderStatus.CONFIRMED);

    return orderDAO.save(order);
}
```

### 2.2 Technical Decisions and Justifications

#### 1. Layered Architecture

**Decision:** Implement a 4-layer architecture: Controller → Service → DAO → Repository.

**Justification:**
- **Separation of concerns**: Each layer has a clear and unique role.
  - **Controller**: Handles HTTP requests/responses and input data validation
  - **Service**: Contains business logic and manages transactions
  - **DAO**: Provides abstraction for data access
  - **Repository**: Spring Data JPA interface that automatically generates SQL queries
- **Testability**: Each layer can be tested independently by mocking its dependencies.
- **Maintainability**: Changes are localized (for example, changing database only affects DAO and Repository layers).

**Why it's suitable for the project:** This architecture facilitates future project evolution and allows easy addition of new features.

#### 2. Use of DTOs (Data Transfer Objects)

**Decision:** Use DTO objects for API requests and responses instead of directly exposing JPA entities.

**Justification:**
- **Security**: Avoids exposing the database's internal structure and JPA relationships.
- **Flexibility**: DTOs can have a different structure from entities (for example, ProductDTO includes `categoryName` while Product entity has a `@ManyToOne` relationship to Category).
- **Validation**: DTOs can have specific validation annotations (@NotBlank, @Size, @Min, etc.) adapted to API needs.
- **Evolution**: The database schema can evolve without breaking the public API.

**Why it's suitable for the project:** This approach guarantees a stable and secure API.

#### 3. BigDecimal for Monetary Amounts

**Decision:** Use `BigDecimal` instead of `double` or `float` for all prices and amounts.

**Justification:**
- **Exact precision**: `double` and `float` types have rounding problems due to their binary floating-point representation.

**Example of problem with double:**
```java
double price = 0.1;
double total = price * 3;
System.out.println(total); // Displays 0.30000000000000004
```

**Solution with BigDecimal:**
```java
BigDecimal price = BigDecimal.valueOf(0.1);
BigDecimal total = price.multiply(BigDecimal.valueOf(3));
System.out.println(total); // Displays 0.30
```

**Why it's suitable for the project:** For an e-commerce application, precision in financial calculations is critical. BigDecimal is the recommended standard solution in Java for monetary amounts.

#### 4. Lombok to Reduce Boilerplate Code

**Decision:** Use the Lombok library to automatically generate getters, setters, constructors, equals, hashCode, and toString.

**Justification:**
- **Readability**: Code is shorter and easier to read.
- **Maintenance**: Less manual code to maintain means fewer potential bugs.
- **Productivity**: Faster development.

**Example:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    // ... other fields
}
```

Without Lombok, this simple class would require about 50 lines of code (getters, setters, constructors, equals, hashCode, toString).

**Why it's suitable for the project:** Lombok allows focusing on business logic rather than repetitive code.

---

## 3. Database Definition

### 3.1 Database Schema

**Database name:** `shop_db`
**DBMS:** PostgreSQL 15

**Entity-Relationship Diagram (ERD):**

```
┌─────────────────────┐
│    categories       │
├─────────────────────┤
│ id (PK)             │
│ name (UNIQUE)       │
│ description         │
└──────────┬──────────┘
           │
           │ 1
           │
           │ N (FK: id_category)
┌──────────▼──────────┐
│    products         │
├─────────────────────┤
│ id (PK)             │
│ name                │
│ description         │
│ price               │
│ quantity_stock      │
│ id_category (FK)    │
└─────────────────────┘


┌─────────────────────┐
│     orders          │
├─────────────────────┤
│ id (PK)             │
│ date                │
│ statut              │
│ total_amount        │
└──────────┬──────────┘
           │
           │ 1
           │
           │ N (FK: id_order)
┌──────────▼──────────┐
│   order_lines       │
├─────────────────────┤
│ id (PK)             │
│ id_order (FK)       │
│ id_product (FK)     │
│ quantity            │
│ unit_price          │
└─────────────────────┘
```

### 3.2 Detailed Table Descriptions

#### Table 1: categories

**Responsibility:** Managed by Product Service

| Column      | Type         | Constraints         | Description                          |
|-------------|--------------|---------------------|--------------------------------------|
| id          | BIGSERIAL    | PRIMARY KEY         | Unique auto-incremented identifier   |
| name        | VARCHAR(100) | NOT NULL, UNIQUE    | Category name                        |
| description | VARCHAR(500) | NULL                | Category description                 |

**Constraints:**
- `PRIMARY KEY (id)`: Guarantees identifier uniqueness
- `UNIQUE (name)`: Prevents duplicate category names

**Justification:**
- **BIGSERIAL for id**: Auto-incremented type that can support billions of categories
- **UNIQUE on name**: Avoids confusion and guarantees that a category is identifiable by its name
- **Optional description**: Not all categories necessarily need a description

---

#### Table 2: products

**Responsibility:** Managed by Product Service

| Column         | Type          | Constraints                  | Description                           |
|----------------|---------------|------------------------------|---------------------------------------|
| id             | BIGSERIAL     | PRIMARY KEY                  | Unique auto-incremented identifier    |
| name           | VARCHAR(200)  | NOT NULL                     | Product name                          |
| description    | VARCHAR(1000) | NULL                         | Detailed product description          |
| price          | DECIMAL(10,2) | NOT NULL, CHECK (price > 0)  | Product unit price                    |
| quantity_stock | INTEGER       | NOT NULL, CHECK (>= 0)       | Available quantity in stock           |
| id_category    | BIGINT        | NOT NULL, FOREIGN KEY        | Reference to category                 |

**Constraints:**
- `PRIMARY KEY (id)`: Guarantees identifier uniqueness
- `FOREIGN KEY (id_category) REFERENCES categories(id) ON DELETE RESTRICT`: Prevents deletion of a category containing products
- `CHECK (price > 0)`: Price must be strictly positive
- `CHECK (quantity_stock >= 0)`: Stock can never be negative

**Indexes:**
- `idx_products_category` on `id_category`: Optimizes "all products in category X" queries
- `idx_products_name` on `name`: Optimizes product name searches

**Justification:**
- **DECIMAL(10,2) for price**: Exact precision for monetary amounts (10 digits total including 2 decimals) → maximum price of 99,999,999.99
- **INTEGER for quantity_stock**: Stock is expressed in whole units (can't have 2.5 laptops)
- **ON DELETE RESTRICT**: Protects against accidental deletion of a category that still contains products
- **Index on id_category**: Category filtering queries are very frequent in an e-commerce application

---

#### Table 3: orders

**Responsibility:** Managed by Order Service

| Column       | Type          | Constraints                           | Description                        |
|--------------|---------------|---------------------------------------|------------------------------------|
| id           | BIGSERIAL     | PRIMARY KEY                           | Unique auto-incremented identifier |
| date         | TIMESTAMP     | NOT NULL, DEFAULT CURRENT_TIMESTAMP   | Order date and time                |
| statut       | VARCHAR(50)   | NOT NULL, CHECK (statut IN (...))     | Order status                       |
| total_amount | DECIMAL(10,2) | NOT NULL, CHECK (total_amount >= 0)   | Order total amount                 |

**Possible status values:**
- `PENDING`: Awaiting validation
- `CONFIRMED`: Confirmed (default status at creation)
- `PROCESSING`: Being processed
- `SHIPPED`: Shipped
- `DELIVERED`: Delivered
- `CANCELLED`: Cancelled

**Constraints:**
- `PRIMARY KEY (id)`: Guarantees identifier uniqueness
- `CHECK (statut IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'))`: Only valid statuses are accepted
- `CHECK (total_amount >= 0)`: Total amount cannot be negative
- `DEFAULT CURRENT_TIMESTAMP`: Creation date is recorded automatically

**Indexes:**
- `idx_orders_date` on `date`: Optimizes queries by period (e.g., December orders)
- `idx_orders_statut` on `statut`: Optimizes filters by status (e.g., all orders in progress)

**Justification:**
- **TIMESTAMP for date**: Allows knowing exactly when the order was created (date and time)
- **CHECK on statut**: Guarantees data integrity at database level
- **Denormalized total_amount**: Total amount is stored directly rather than calculated each time (performance), even though it can be calculated from order lines

---

#### Table 4: order_lines

**Responsibility:** Managed by Order Service

| Column      | Type          | Constraints                     | Description                             |
|-------------|---------------|---------------------------------|-----------------------------------------|
| id          | BIGSERIAL     | PRIMARY KEY                     | Unique auto-incremented identifier      |
| id_order    | BIGINT        | NOT NULL, FOREIGN KEY           | Reference to order                      |
| id_product  | BIGINT        | NOT NULL, FOREIGN KEY           | Reference to product                    |
| quantity    | INTEGER       | NOT NULL, CHECK (quantity > 0)  | Ordered quantity                        |
| unit_price  | DECIMAL(10,2) | NOT NULL, CHECK (unit_price >=0)| Unit price at time of order             |

**Constraints:**
- `PRIMARY KEY (id)`: Guarantees identifier uniqueness
- `FOREIGN KEY (id_order) REFERENCES orders(id) ON DELETE CASCADE`: If an order is deleted, all its lines are automatically deleted
- `FOREIGN KEY (id_product) REFERENCES products(id)`: Guarantees that referenced product exists
- `CHECK (quantity > 0)`: Quantity must be strictly positive (no zero or negative quantity)
- `CHECK (unit_price >= 0)`: Unit price cannot be negative

**Indexes:**
- `idx_order_lines_order` on `id_order`: Optimizes main query "retrieve all lines of an order"
- `idx_order_lines_product` on `id_product`: Optimizes queries "which orders contain this product"

**Justification:**
- **Stored unit_price**: Price is captured at order time and remains fixed even if product price changes later (price history)
- **ON DELETE CASCADE**: Management simplification: if an order is deleted, its lines are automatically deleted
- **FOREIGN KEY to products**: Although services are separated, the constraint guarantees referential integrity

---

### 3.3 Relationships Between Tables

#### Relationship 1: Categories → Products (One-to-Many)

```
categories.id (1) ←──── FK ────→ (N) products.id_category
```

- **Cardinality**: One category can contain multiple products (1:N)
- **Implementation**: Foreign key `id_category` in `products` table
- **Deletion policy**: `ON DELETE RESTRICT` - impossible to delete a category containing products

**Justification:** This relationship allows organizing products into categories to facilitate navigation and filtering. The RESTRICT policy protects against accidental deletion of used categories.

#### Relationship 2: Orders → Order_Lines (One-to-Many)

```
orders.id (1) ←──── FK ────→ (N) order_lines.id_order
```

- **Cardinality**: One order contains multiple order lines (1:N)
- **Implementation**: Foreign key `id_order` in `order_lines` table
- **Deletion policy**: `ON DELETE CASCADE` - if an order is deleted, all its lines are also deleted

**Justification:** An order is composed of multiple lines (one per ordered product). The CASCADE policy simplifies management: deleting an order automatically deletes its details.

#### Relationship 3: Products → Order_Lines (One-to-Many - Reference)

```
products.id (1) ←──── FK ────→ (N) order_lines.id_product
```

- **Cardinality**: One product can appear in multiple order lines (1:N)
- **Implementation**: Foreign key `id_product` in `order_lines` table
- **Note**: This is a simple reference, not a true JPA relationship because services are separated

**Justification:** This reference allows knowing which product was ordered. The price is duplicated in `order_lines.unit_price` to preserve history (even if product price changes later).

---

## 4. Detailed Operation Explanation

This section describes step-by-step how the application works, showing how different parts (services, layers, database) interact to meet requirements.

### 4.1 General Architecture

The application is based on a **microservices architecture** with two independent services:

```
┌─────────────────────────────────────────────────────────────┐
│                     Client (Postman/cURL)                   │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
┌────────▼─────────┐   ┌────────▼─────────┐
│ Product Service  │   │  Order Service   │
│   Port: 8081     │◄──┤   Port: 8082     │
│                  │   │                  │
│ Tables:          │   │ Tables:          │
│ - categories     │   │ - orders         │
│ - products       │   │ - order_lines    │
└────────┬─────────┘   └────────┬─────────┘
         │                      │
         └──────────┬───────────┘
                    │
         ┌──────────▼──────────┐
         │      shop_db        │
         │    (PostgreSQL)     │
         └─────────────────────┘
```

**Key points:**
- Both services **share the same database** but manage different tables
- Order Service **communicates with Product Service** via REST HTTP calls
- Each service respects a **layered architecture** (Controller → Service → DAO → Repository → Database)

### 4.2 Layered Architecture of Each Service

Each service (Product and Order) follows the same layered structure:

```
Client (Postman)
      │
      │ HTTP Request (JSON)
      ▼
┌─────────────────────────────────────────────┐
│         CONTROLLER LAYER                    │
│  - Receives HTTP requests                   │
│  - Validates data (@Valid)                  │
│  - Converts JSON ↔ DTO                      │
│  - Returns HTTP responses                   │
└──────────────┬──────────────────────────────┘
               │ DTO (Data Transfer Object)
               ▼
┌─────────────────────────────────────────────┐
│          SERVICE LAYER                      │
│  - Contains business logic                  │
│  - Manages transactions (@Transactional)    │
│  - Validates business rules                 │
│  - Converts DTO ↔ Entity                    │
└──────────────┬──────────────────────────────┘
               │ Entity (JPA)
               ▼
┌─────────────────────────────────────────────┐
│           DAO LAYER                         │
│  - Abstraction for data access              │
│  - Delegates to repositories                │
│  - Can combine multiple repositories        │
└──────────────┬──────────────────────────────┘
               │ Entity
               ▼
┌─────────────────────────────────────────────┐
│        REPOSITORY LAYER                     │
│  - Spring Data JPA interface                │
│  - Automatically generates SQL queries      │
│  - CRUD methods + custom methods            │
└──────────────┬──────────────────────────────┘
               │ SQL
               ▼
┌─────────────────────────────────────────────┐
│         DATABASE (PostgreSQL)               │
│  - Tables: categories, products,            │
│            orders, order_lines              │
└─────────────────────────────────────────────┘
```

### 4.3 Workflow 1: Creating a Category

**Scenario:** A user creates a new category "Electronics"

**Detailed steps:**

```
1. CLIENT
   └─> Sends POST request http://localhost:8081/api/categories
       Body: {"name": "Electronics", "description": "Electronic devices"}

2. CONTROLLER (CategoryController)
   ├─> Receives HTTP request
   ├─> Validates DTO with @Valid
   │   ├─> @NotBlank verifies name is not empty
   │   └─> @Size verifies name <= 100 characters
   └─> Calls categoryService.createCategory(categoryDTO)

3. SERVICE (CategoryService)
   ├─> Verifies name uniqueness
   │   └─> Calls categoryDAO.existsByName("Electronics")
   ├─> If name already exists → throws IllegalArgumentException
   ├─> Converts CategoryDTO to Category entity
   ├─> Calls categoryDAO.save(category) with @Transactional
   └─> Converts saved entity to CategoryDTO

4. DAO (CategoryDAO)
   └─> Delegates to categoryRepository.save(category)

5. REPOSITORY (CategoryRepository extends JpaRepository)
   ├─> Spring Data JPA automatically generates SQL query:
   │   INSERT INTO categories (name, description)
   │   VALUES ('Electronics', 'Electronic devices')
   │   RETURNING id;
   └─> Returns entity with generated ID (e.g., id=1)

6. DATABASE (PostgreSQL)
   ├─> Executes INSERT
   ├─> Verifies UNIQUE constraint on name
   ├─> Generates auto-incremented ID (BIGSERIAL)
   └─> Returns created record

7. RETURN (up through layers)
   Repository → DAO → Service → Controller

8. CONTROLLER
   └─> Returns ResponseEntity with:
       - HTTP Code: 201 Created
       - Body: {"id": 1, "name": "Electronics", "description": "Electronic devices"}

9. CLIENT
   └─> Receives JSON response with created category ID
```

### 4.4 Workflow 2: Creating a Product

**Scenario:** A user creates a new product "Laptop" in category "Electronics" (id=1)

**Detailed steps:**

```
1. CLIENT
   └─> POST http://localhost:8081/api/products
       Body: {
         "name": "Laptop",
         "price": 1299.99,
         "quantityStock": 10,
         "categoryId": 1
       }

2. CONTROLLER (ProductController)
   ├─> Validates DTO
   │   ├─> @NotBlank on name
   │   ├─> @DecimalMin(0.0) on price
   │   ├─> @Min(0) on quantityStock
   │   └─> @NotNull on categoryId
   └─> Calls productService.createProduct(productDTO)

3. SERVICE (ProductService)
   ├─> Verifies category exists
   │   ├─> Calls categoryDAO.findById(1)
   │   └─> If not found → throws IllegalArgumentException
   ├─> Converts ProductDTO to Product entity
   ├─> Associates category: product.setCategory(category)
   └─> Calls productDAO.save(product) with @Transactional

4. DAO (ProductDAO)
   └─> Delegates to productRepository.save(product)

5. REPOSITORY (ProductRepository)
   └─> Generates SQL:
       INSERT INTO products (name, price, quantity_stock, id_category)
       VALUES ('Laptop', 1299.99, 10, 1)
       RETURNING id;

6. DATABASE
   ├─> Verifies FOREIGN KEY: id_category=1 exists in categories
   ├─> Verifies CHECK (price > 0)
   ├─> Verifies CHECK (quantity_stock >= 0)
   ├─> Generates id=1
   └─> Inserts record

7. RETURN
   Service → Controller

8. CONTROLLER
   └─> Enriches DTO with category name
   └─> Returns 201 Created with:
       {
         "id": 1,
         "name": "Laptop",
         "price": 1299.99,
         "quantityStock": 10,
         "categoryId": 1,
         "categoryName": "Electronics"  ← Added for API
       }
```

### 4.5 Workflow 3: Creating an Order (Inter-Service Communication)

**Scenario:** A user creates an order for 2 Laptops (id=1)

**Detailed steps with inter-service communication:**

```
1. CLIENT
   └─> POST http://localhost:8082/api/orders
       Body: {
         "items": [
           {"idProduct": 1, "quantity": 2}
         ]
       }

2. ORDER SERVICE - CONTROLLER (OrderController)
   └─> Validates DTO and calls orderService.createOrder(request)

3. ORDER SERVICE - SERVICE (OrderService)
   └─> @Transactional starts a transaction

   For each product in items:

   ┌──────────────────────────────────────────────────────┐
   │ 3.1 COMMUNICATION WITH PRODUCT SERVICE               │
   └──────────────────────────────────────────────────────┘

   ├─> Calls ProductServiceClient.getProduct(1)
   │   │
   │   ├─> WebClient sends:
   │   │   GET http://localhost:8081/api/products/1
   │   │
   │   ├─> PRODUCT SERVICE processes request
   │   │   └─> ProductController → ProductService → ProductDAO → Repository
   │   │   └─> SELECT * FROM products WHERE id = 1;
   │   │
   │   └─> Returns:
   │       {
   │         "id": 1,
   │         "name": "Laptop",
   │         "price": 1299.99,
   │         "quantityStock": 10,
   │         "categoryId": 1
   │       }
   │
   ├─> Verifies available stock
   │   └─> if (product.quantityStock < 2) → throws IllegalArgumentException
   │   └─> 10 >= 2 OK
   │
   ├─> Calls ProductServiceClient.decreaseStock(1, 2)
   │   │
   │   ├─> WebClient sends:
   │   │   POST http://localhost:8081/api/products/decrease-stock
   │   │   Body: {"productId": 1, "quantity": 2}
   │   │
   │   ├─> PRODUCT SERVICE processes request
   │   │   └─> ProductController → ProductService → ProductDAO
   │   │   └─> UPDATE products
   │   │       SET quantity_stock = quantity_stock - 2
   │   │       WHERE id = 1 AND quantity_stock >= 2;
   │   │   └─> New value: quantity_stock = 8
   │   │
   │   └─> Returns: true (success)
   │
   ├─> Creates OrderLine
   │   └─> idProduct: 1
   │   └─> quantity: 2
   │   └─> unitPrice: 1299.99  ← Price captured at this moment
   │   └─> order.addOrderLine(orderLine)
   │
   └─> Calculates total
       └─> totalAmount = 1299.99 × 2 = 2599.98

4. ORDER SERVICE - DAO (OrderDAO)
   └─> Saves order and its lines
       ├─> INSERT INTO orders (date, statut, total_amount)
       │   VALUES (CURRENT_TIMESTAMP, 'CONFIRMED', 2599.98);
       │   → Generates id=1
       │
       └─> INSERT INTO order_lines (id_order, id_product, quantity, unit_price)
           VALUES (1, 1, 2, 1299.99);
           → Generates id=1

5. DATABASE
   ├─> Verifies FK: id_order=1 exists in orders
   ├─> Verifies FK: id_product=1 exists in products
   ├─> Verifies CHECK (quantity > 0)
   └─> Commits transaction

6. RETURN
   └─> 201 Created with:
       {
         "id": 1,
         "date": "2025-12-11T10:30:00",
         "statut": "CONFIRMED",
         "totalAmount": 2599.98,
         "orderLines": [
           {
             "id": 1,
             "orderId": 1,
             "idProduct": 1,
             "quantity": 2,
             "unitPrice": 1299.99,
             "lineTotal": 2599.98
           }
         ]
       }
```

### How to Start the Application

**Prerequisites:**
- Java 17+
- PostgreSQL 15+
- Maven 3.8+

**1. Create the database:**
```bash
psql -U postgres
\i database/create_databases.sql
```

**2. Start Product Service:**
```bash
cd product-service/src
mvn spring-boot:run
```
-> http://localhost:8081

**3. Start Order Service:**
```bash
cd order-service/src
mvn spring-boot:run
```
-> http://localhost:8082

**4. Test with cURL:**
```bash
# Create a category
curl -X POST http://localhost:8081/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic devices"}'

# Create a product
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":1299.99,"quantityStock":10,"categoryId":1}'

# Create an order
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"idProduct":1,"quantity":1}]}'
```

### Project Structure

```
shop/
├── product-service/src/
│   ├── main/java/com/shop/productservice/
│   │   ├── controller/     # REST API
│   │   ├── service/        # Business logic
│   │   ├── dao/            # Data access
│   │   ├── repository/     # Spring Data JPA
│   │   ├── entity/         # JPA entities
│   │   └── dto/            # Data Transfer Objects
│   └── main/resources/
│       └── application.properties
│
├── order-service/src/
│   ├── main/java/com/shop/orderservice/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dao/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── client/         # Communication with Product Service
│   │   └── config/         # WebClient configuration
│   └── main/resources/
│       └── application.properties
│
├── database/
│   └── create_databases.sql
│
└── README.md
```

---
