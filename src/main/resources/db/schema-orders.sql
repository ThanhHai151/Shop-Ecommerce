-- ============================================
-- Schema for Orders Database (computershop_orders)
-- Contains: Orders, OrderDetails, Carts, CartItems, Payments
-- ============================================

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'computershop_orders')
BEGIN
    CREATE DATABASE computershop_orders;
END
GO

USE computershop_orders;
GO

-- ============================================
-- Orders Table
-- ============================================
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL DROP TABLE dbo.orders;
CREATE TABLE dbo.orders (
    order_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    order_date DATETIME NOT NULL DEFAULT GETDATE(),
    status VARCHAR(50) NULL DEFAULT 'pending',
    shipping_address NVARCHAR(MAX) NULL,
    payment_method VARCHAR(50) NULL,
    notes NVARCHAR(MAX) NULL
);
GO

-- ============================================
-- Order Details Table
-- ============================================
IF OBJECT_ID(N'dbo.order_details', N'U') IS NOT NULL DROP TABLE dbo.order_details;
CREATE TABLE dbo.order_details (
    order_detail_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_order_details_orders FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
    -- Note: product_id references products in main database
);
GO

-- ============================================
-- Carts Table
-- ============================================
IF OBJECT_ID(N'dbo.carts', N'U') IS NOT NULL DROP TABLE dbo.carts;
CREATE TABLE dbo.carts (
    cart_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================
-- Cart Items Table
-- ============================================
IF OBJECT_ID(N'dbo.cart_items', N'U') IS NOT NULL DROP TABLE dbo.cart_items;
CREATE TABLE dbo.cart_items (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_cart_items_carts FOREIGN KEY (cart_id) REFERENCES dbo.carts(cart_id) ON DELETE CASCADE
    -- Note: product_id references products in main database
);
GO

-- ============================================
-- Payment Transactions Table
-- ============================================
IF OBJECT_ID(N'dbo.payment_transactions', N'U') IS NOT NULL DROP TABLE dbo.payment_transactions;
CREATE TABLE dbo.payment_transactions (
    transaction_id INT IDENTITY(1,1) PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    transaction_date DATETIME NOT NULL DEFAULT GETDATE(),
    transaction_ref VARCHAR(255) NULL,
    CONSTRAINT FK_payment_transactions_orders FOREIGN KEY (order_id) REFERENCES dbo.orders(order_id)
);
GO

-- ============================================
-- Password Reset Tokens Table
-- ============================================
IF OBJECT_ID(N'dbo.password_reset_tokens', N'U') IS NOT NULL DROP TABLE dbo.password_reset_tokens;
CREATE TABLE dbo.password_reset_tokens (
    token_id INT IDENTITY(1,1) PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);
GO

PRINT 'Orders database schema created successfully!';
GO
