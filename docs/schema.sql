-- =====================================================================
-- schema.sql: nguồn sự thật của database warehouse_db (MySQL 8)
-- Entity/bảng Exposed phải khớp tuyệt đối với file này.
-- Không dùng SchemaUtils.create*; schema do file này quản lý.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS warehouse_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE warehouse_db;

-- Bảng 1: users (tài khoản và phân quyền)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NULL,
    password_hash VARCHAR(255) NOT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    temp_password_expires_at DATETIME NULL,
    password_changed_at DATETIME NULL,
    role ENUM('ADMIN', 'WAREHOUSE_STAFF')
        NOT NULL DEFAULT 'WAREHOUSE_STAFF',
    status ENUM('ACTIVE', 'INACTIVE')
        NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng 2: refresh_tokens (quản lý phiên đăng nhập)
CREATE TABLE refresh_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    refresh_token VARCHAR(500) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- Bảng 3: categories (danh mục sản phẩm)
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng 4: suppliers (nhà cung cấp)
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    address TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng 5: products (sản phẩm)
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(100) NULL UNIQUE,
    product_name VARCHAR(150) NOT NULL,
    image_url VARCHAR(255) NULL,
    category_id INT NULL,
    supplier_id INT NULL,
    unit VARCHAR(50) NOT NULL,
    cost_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    sale_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    min_stock INT NOT NULL DEFAULT 0,
    max_stock INT NOT NULL DEFAULT 1000,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
        ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL,
    CHECK (cost_price >= 0),
    CHECK (sale_price >= 0),
    CHECK (min_stock >= 0),
    CHECK (max_stock >= min_stock)
);

-- Bảng 6: warehouses (kho và chi nhánh)
CREATE TABLE warehouses (
    warehouse_id INT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL,
    location TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng 7: inventory (tồn kho theo kho và lô hàng)
CREATE TABLE inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id INT NOT NULL,
    product_id INT NOT NULL,
    -- Chuỗi rỗng đại diện cho hàng không quản lý theo lô
    batch_code VARCHAR(100) NOT NULL DEFAULT '',
    manufacture_date DATE NULL,
    expiry_date DATE NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    -- Cột sinh tự động: KHÔNG được INSERT/UPDATE
    available_quantity INT GENERATED ALWAYS AS
        (stock_quantity - reserved_quantity) STORED,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE KEY uq_inventory_batch
        (warehouse_id, product_id, batch_code),
    INDEX idx_inventory_product (product_id),
    INDEX idx_inventory_expiry (expiry_date),
    CHECK (stock_quantity >= 0),
    CHECK (reserved_quantity >= 0),
    CHECK (reserved_quantity <= stock_quantity)
);

-- Bảng 8: transactions (phiếu nhập, xuất, chuyển, kiểm kê, điều chỉnh)
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id INT NOT NULL,
    target_warehouse_id INT NULL,
    supplier_id INT NULL,
    user_id INT NOT NULL,          -- người lập phiếu
    approved_by INT NULL,          -- người duyệt
    transaction_type ENUM(
        'IMPORT',
        'EXPORT',
        'TRANSFER',
        'STOCKTAKE',
        'ADJUSTMENT'
    ) NOT NULL,
    transaction_subtype ENUM(
        'PURCHASE',
        'SALE',
        'DISPOSAL',
        'INTERNAL',
        'COUNT',
        'SURPLUS',
        'SHORTAGE',
        'DAMAGE',
        'OTHER'
    ) NULL,
    status ENUM(
        'DRAFT',
        'PENDING',
        'APPROVED',
        'REJECTED',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'DRAFT',
    note TEXT NULL,
    rejection_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME NULL,
    approved_at DATETIME NULL,
    completed_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    FOREIGN KEY (target_warehouse_id) REFERENCES warehouses(warehouse_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
        ON DELETE SET NULL,
    INDEX idx_transaction_type_status
        (transaction_type, status),
    INDEX idx_transaction_created (created_at),
    INDEX idx_transaction_warehouse (warehouse_id)
);

-- Bảng 9: transaction_details (chi tiết phiếu)
CREATE TABLE transaction_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    product_id INT NOT NULL,
    batch_code VARCHAR(100) NOT NULL DEFAULT '',
    manufacture_date DATE NULL,
    expiry_date DATE NULL,
    quantity INT NOT NULL,
    -- Giá nhập, giá xuất hoặc giá trị tham chiếu của dòng phiếu
    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    -- Dùng cho nghiệp vụ kiểm kê
    system_quantity INT NULL,
    actual_quantity INT NULL,
    -- Cột sinh tự động: KHÔNG được INSERT/UPDATE
    difference_quantity INT GENERATED ALWAYS AS (
        CASE
            WHEN actual_quantity IS NOT NULL
             AND system_quantity IS NOT NULL
            THEN actual_quantity - system_quantity
            ELSE NULL
        END
    ) STORED,
    note TEXT NULL,
    FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)
        REFERENCES products(product_id),
    INDEX idx_detail_product (product_id),
    INDEX idx_detail_batch (product_id, batch_code),
    CHECK (quantity > 0),
    CHECK (unit_price >= 0),
    CHECK (system_quantity IS NULL OR system_quantity >= 0),
    CHECK (actual_quantity IS NULL OR actual_quantity >= 0)
);

-- Bảng 10: knowledge_documents (tài liệu RAG và lịch sử hội thoại AI)
CREATE TABLE knowledge_documents (
    doc_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    category ENUM(
        'POLICY',
        'GUIDE',
        'FAQ',
        'CHAT_HISTORY'
    ) NOT NULL DEFAULT 'GUIDE',
    user_id INT NULL,
    session_id VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL,
    INDEX idx_document_category (category, is_active),
    INDEX idx_document_session (session_id)
);

-- Bảng 11: vector_embeddings (dữ liệu vector phục vụ RAG)
CREATE TABLE vector_embeddings (
    embedding_id INT AUTO_INCREMENT PRIMARY KEY,
    source_type ENUM(
        'PRODUCT',
        'DOCUMENT',
        'INVENTORY'
    ) NOT NULL,
    source_id INT NOT NULL,
    chunk_index INT NOT NULL DEFAULT 0,
    chunk_text TEXT NOT NULL,
    embedding_vector LONGTEXT NOT NULL,
    embedding_model VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_source_chunk
        (source_type, source_id, chunk_index),
    INDEX idx_embedding_source (source_type, source_id)
);

-- =====================================================================
-- PATCH Sprint 2 (chạy sau khi tạo các bảng trên)
-- Phục vụ sổ kho (UC36): lọc theo status và completed_at
-- =====================================================================
CREATE INDEX idx_transaction_status_completed
    ON transactions (status, completed_at);
