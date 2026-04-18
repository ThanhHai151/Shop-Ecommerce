-- ============================================
-- Schema for Main Database (computershop_main)
-- Contains: Roles, Users, Categories, Images, Products
-- ============================================

-- Create database if not exists
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'computershop_main')
BEGIN
    CREATE DATABASE computershop_main;
END
GO

USE computershop_main;
GO

-- ============================================
-- Roles Table
-- ============================================
IF OBJECT_ID(N'dbo.roles', N'U') IS NOT NULL DROP TABLE dbo.roles;
CREATE TABLE dbo.roles (
    role_id INT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);
GO

-- ============================================
-- Users Table
-- ============================================
IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL DROP TABLE dbo.users;
CREATE TABLE dbo.users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NULL UNIQUE,
    roleid INT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_users_roles FOREIGN KEY (roleid) REFERENCES dbo.roles(role_id)
);
GO

-- ============================================
-- Categories Table
-- ============================================
IF OBJECT_ID(N'dbo.categories', N'U') IS NOT NULL DROP TABLE dbo.categories;
CREATE TABLE dbo.categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(MAX) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- ============================================
-- Images Table
-- ============================================
IF OBJECT_ID(N'dbo.images', N'U') IS NOT NULL DROP TABLE dbo.images;
CREATE TABLE dbo.images (
    image_id INT IDENTITY(1,1) PRIMARY KEY,
    image_url NVARCHAR(MAX) NOT NULL
);
GO

-- ============================================
-- Products Table
-- ============================================
IF OBJECT_ID(N'dbo.products', N'U') IS NOT NULL DROP TABLE dbo.products;
CREATE TABLE dbo.products (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NULL,
    price DECIMAL(18,2) NOT NULL,
    stock_quantity INT NOT NULL,
    category_id INT NULL,
    image_id INT NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_products_categories FOREIGN KEY (category_id) REFERENCES dbo.categories(category_id),
    CONSTRAINT FK_products_images FOREIGN KEY (image_id) REFERENCES dbo.images(image_id)
);
GO

-- ============================================
-- Initial Data Seeding
-- ============================================

-- Insert roles
INSERT INTO dbo.roles (role_id, role_name) VALUES
(1, 'admin'),
(2, 'customer'),
(3, 'staff'),
(4, 'supplier');
GO

PRINT 'Main database schema created successfully!';
GO
