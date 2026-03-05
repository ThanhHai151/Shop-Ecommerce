# 🖥️ JSB ComputerShop - E-Commerce Platform

Hệ thống thương mại điện tử bán linh kiện máy tính được xây dựng với Spring Boot và Thymeleaf.

## 📋 Mục lục
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Cài đặt và Chạy ứng dụng](#-cài-đặt-và-chạy-ứng-dụng)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Tính năng](#-tính-năng)

---

## 🔧 Yêu cầu hệ thống

Trước khi bắt đầu, đảm bảo máy tính của bạn đã cài đặt:

### 1. **Java Development Kit (JDK)**
- **Phiên bản**: JDK 17 trở lên
- **Tải về**: [Eclipse Adoptium](https://adoptium.net/) hoặc [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- **Kiểm tra**:
  ```bash
  java -version
  ```
  Kết quả mong đợi: `java version "17.0.x"` hoặc cao hơn

### 2. **Apache Maven**
- **Phiên bản**: 3.6+ (được tích hợp sẵn trong project qua Maven Wrapper)
- **Kiểm tra**:
  ```bash
  mvn -version
  ```

### 3. **Microsoft SQL Server**
- **Phiên bản**: SQL Server 2016 trở lên hoặc SQL Server Express
- **Tải về**: [SQL Server Express](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)
- **Công cụ quản lý** (tùy chọn): 
  - [SQL Server Management Studio (SSMS)](https://aka.ms/ssmsfullsetup)
  - [Azure Data Studio](https://docs.microsoft.com/en-us/sql/azure-data-studio/download-azure-data-studio)

### 4. **IDE (Integrated Development Environment)**
Chọn một trong các IDE sau:
- **Visual Studio Code** + [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- **IntelliJ IDEA** (Community hoặc Ultimate)
- **Eclipse IDE for Enterprise Java**

---

## Công nghệ sử dụng

### Backend
- **Spring Boot 3.5.6** - Framework chính
- **Spring Data JPA** - ORM và Database Access
- **Spring Security** - Bảo mật và xác thực
- **Hibernate** - JPA Implementation
- **Thymeleaf** - Template Engine

### Database
- **Microsoft SQL Server** - Cơ sở dữ liệu quan hệ
- **HikariCP** - Connection Pooling

### Frontend
- **HTML5/CSS3** - Giao diện người dùng
- **JavaScript** - Tương tác client-side
- **Thymeleaf Templates** - Server-side rendering

---

## 🚀 Cài đặt và Chạy ứng dụng

### Bước 1: Clone Repository

```bash
git clone https://github.com/hayamij/JSB-ComputerShop.git
cd JSB-ComputerShop
```

### Bước 2: Cấu hình Database

#### 2.1. Tạo Database

Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**, sau đó chạy lệnh SQL:

```sql
CREATE DATABASE computershop;
GO
```

#### 2.2. Chạy Script khởi tạo

Mở file `database.sql` trong thư mục gốc của project và thực thi toàn bộ script để tạo tables và dữ liệu mẫu:

```sql
-- Chạy file database.sql
-- File này sẽ tạo các bảng: users, roles, categories, products, images, orders, order_details, etc.
```

Hoặc thực thi trực tiếp từ command line:

```bash
sqlcmd -S localhost -d computershop -i database.sql
```

#### 2.3. Cấu hình kết nối Database

Mở file `src/main/resources/application.properties` và cập nhật thông tin kết nối:

```properties
# Database Configuration
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=computershop;encrypt=true;trustServerCertificate=true
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

**Lưu ý**: 
- Thay `YOUR_USERNAME` và `YOUR_PASSWORD` bằng thông tin đăng nhập SQL Server của bạn
- Nếu sử dụng **Windows Authentication**, xóa dòng username/password và thêm:
  ```properties
  spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=computershop;encrypt=true;trustServerCertificate=true;integratedSecurity=true;authenticationScheme=nativeAuthentication
  ```

### Bước 3: Build Project

Sử dụng Maven để build project:

#### Trên Windows:
```bash
mvnw.cmd clean install
```

#### Trên Linux/Mac:
```bash
./mvnw clean install
```

Hoặc nếu đã cài Maven globally:
```bash
mvn clean install
```

### Bước 4: Chạy ứng dụng

#### Cách 1: Chạy bằng IDE

##### **Visual Studio Code**
1. Mở project trong VS Code
2. Đảm bảo đã cài [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
3. Mở file `src/main/java/com/computershop/main/MainApplication.java`
4. Click nút **Run** hoặc nhấn `F5`
5. Hoặc click chuột phải vào file → chọn **Run Java**

##### **IntelliJ IDEA**
1. Mở project (File → Open → chọn thư mục project)
2. Chờ IntelliJ import Maven dependencies
3. Tìm file `MainApplication.java`
4. Click chuột phải → **Run 'MainApplication.main()'**
5. Hoặc nhấn tổ hợp phím `Shift + F10`

##### **Eclipse IDE**
1. Import project: File → Import → Maven → Existing Maven Projects
2. Chọn thư mục project
3. Tìm class `MainApplication.java`
4. Click chuột phải → **Run As** → **Java Application**

#### Cách 2: Chạy bằng Maven Command

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

#### Cách 3: Chạy file JAR

```bash
# Build JAR file
mvnw.cmd clean package

# Chạy JAR
java -jar target/main-0.0.1-SNAPSHOT.jar
```

### Bước 5: Truy cập ứng dụng

Sau khi ứng dụng khởi động thành công (xem log `Started MainApplication in X seconds`), truy cập:

- **Trang chủ**: http://localhost:8080
- **Danh sách sản phẩm**: http://localhost:8080/products
- **Đăng nhập**: http://localhost:8080/login
- **Admin**: http://localhost:8080/admin (yêu cầu quyền admin)

### Bước 6: Tài khoản mẫu

Sau khi chạy script `database.sql`, bạn có thể đăng nhập với các tài khoản sau:

**Admin:**
- Email: `admin@computershop.com`
- Password: `admin123`

**User:**
- Email: `user@computershop.com`
- Password: `user123`

---

## 📂 Cấu trúc dự án

```
JSB-ComputerShop/
├── src/
│   ├── main/
│   │   ├── java/com/computershop/main/
│   │   │   ├── controllers/          # REST Controllers & MVC Controllers
│   │   │   ├── entities/             # JPA Entities (Models)
│   │   │   ├── repositories/         # Spring Data JPA Repositories
│   │   │   ├── services/             # Business Logic Layer
│   │   │   └── MainApplication.java  # Spring Boot Entry Point
│   │   └── resources/
│   │       ├── application.properties # Cấu hình ứng dụng
│   │       ├── static/               # CSS, JavaScript, Images
│   │       │   ├── CSS/
│   │       │   ├── JavaScript/
│   │       │   └── Images/
│   │       └── templates/            # Thymeleaf HTML Templates
│   │           ├── admin/            # Admin pages
│   │           ├── cart/             # Shopping cart pages
│   │           ├── header.html
│   │           ├── footer.html
│   │           ├── home.html
│   │           └── products.html
│   └── test/                         # Unit Tests
├── database.sql                      # Database initialization script
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

---

## ✨ Tính năng

### Khách hàng (Customer)
- ✅ Xem danh sách sản phẩm với bộ lọc và tìm kiếm
- ✅ Xem chi tiết sản phẩm
- ✅ Thêm sản phẩm vào giỏ hàng
- ✅ Quản lý giỏ hàng (thêm, xóa, cập nhật số lượng)
- ✅ Đặt hàng và thanh toán
- ✅ Xem lịch sử đơn hàng
- ✅ Quản lý thông tin cá nhân

### Quản trị viên (Admin)
- ✅ Dashboard thống kê
- ✅ Quản lý sản phẩm (CRUD)
- ✅ Quản lý danh mục sản phẩm
- ✅ Quản lý đơn hàng
- ✅ Quản lý người dùng
- ✅ Quản lý tồn kho
- ✅ Báo cáo doanh thu

---

## 🐛 Xử lý sự cố

### Lỗi kết nối Database

**Lỗi**: `Login failed for user 'xxx'`

**Giải pháp**:
1. Kiểm tra username và password trong `application.properties`
2. Đảm bảo SQL Server đang chạy:
   ```bash
   # Windows Services
   services.msc → Tìm "SQL Server" → Start
   ```
3. Kiểm tra SQL Server Authentication mode (Mixed Mode)

### Lỗi Port đã được sử dụng

**Lỗi**: `Port 8080 was already in use`

**Giải pháp**:
1. Đổi port trong `application.properties`:
   ```properties
   server.port=8081
   ```
2. Hoặc kill process đang dùng port 8080

### Lỗi Maven dependencies

**Giải pháp**:
```bash
mvnw.cmd clean install -U
```

---

## �📝 Development Notes

- Controllers handle HTTP requests and return Thymeleaf views
- Services contain business logic and transaction management  
- Repositories use Spring Data JPA for database operations
- Entities are mapped to SQL Server tables with proper relationships
- CSS is separated from HTML templates for maintainability

---

## 📄 License

This project is licensed under the MIT License.

---

**Note**: Đây là project học tập, không sử dụng cho mục đích thương mại.

---

This project builds a full-featured e-commerce platform using Spring Boot (REST API) and Thymeleaf. It supports User flows (browse, cart, secure checkout, order tracking) and an Admin module for comprehensive management (products, inventory, orders, users, reporting).

FINAL PROJECT REQUIREMENTS
1. System Overview
Objective: Build an e-commerce website that allows users to browse products, shopping carts, payments, and manage orders; Admins manage products, inventory, orders, users, and reports.
Proposed architecture:
Backend: Spring Boot (RESTful API), Spring Data JPA (Hibernate), Spring Security (JWT or session), Spring MVC if using server-side rendering.
Database: MySQL/PostgreSQL/SQLServer
Frontend: Option A: SPA (React/Vue) calls REST API — recommended. Option B: Thymeleaf (server-side) if you want pure Java.
Image file storage: local (dev) or S3/GCS (prod).
Payment: integrated payment gateway (VNPay, Momo, Stripe) — webhook processing.
Environment: Docker, CI/CD (GitHub Actions/GitLab CI), deploy Kubernetes or VPS.
2. Roles & Permissions
Anonymous (Guest): browse products, search, view details, add to cart (temporarily local), create account.
User (Customer): all customer rights + order, payment, view history, manage profile, reviews, wishlist.
Seller (if multi-vendor): (optional) manage their own products.
Admin: manage products, categories, inventory, orders, users, promotions, reports, system settings.
Support/Staff: process orders, respond to customers (depending on design).
3. FUNCTIONAL REQUIREMENTS — USER PART (User / Customer)
3.1. Register & Login
Register with email + password, email authentication (token).
Login with email/password
Forgot password: send reset email (short-term token).
2-step authentication (2FA) — optional.
3.2. User profile (Profile)
View/edit: name, email, phone number, address (multiple addresses), avatar.
Save default shipping address, billing address.
Activity history (orders, reviews).
3.3. Browse & Search products
Product hierarchy category.
Filters: price, brand, color, size, stock status, rating, promotion.
Sort: popular, newest, price (ascending/decreasing), rating.
Full-text search (Elasticsearch or MySQL fulltext).
List page + pagination + lazy loading.
3.4. Product detail page
Images (gallery), description, specifications, variants (color/size), SKU, inventory by variant, original price & promotional price, reviews & comments, questions & answers.
Add to cart (select variant, quantity), “Buy now”.
3.5. Cart
Save cart for logged-in users (including session memory for guests).
Update quantity, delete item, apply discount code.
Check inventory when updating quantity/checkout.
3.6. Checkout
Multi-step checkout: delivery information → shipping → payment → confirmation.
Support multiple payment methods: cash on delivery (COD), payment gateway (VNPay/Momo/Stripe), bank transfer.
Secure transaction processing (3DS if needed), save transaction logs.
Support invoice / print invoice (PDF).
3.7. Order Management (User)
View order list, status (Pending, Paid, Processing, Shipped, Delivered, Cancelled, Returned, Refunded).
View order details, status history, tracking number (if any).
Request to cancel order, request to exchange/return (with reason & processing status).
Rate product after receiving (rating + comment). Regulations: only the purchasing user can rate.
3.8. Discount code / Voucher
Apply discount code at checkout, check conditions (expiration date, number of times, minimum value, product type).
3.9. Notification
Email/SMS/Notification in-app for order status, promotions, reset password.
Configure email template.
3.10. Wishlist / Favorites
Save favorite products, quickly transfer to cart.
3.11. Chat / Support
Chat widget (live chat) or simple contact form + ticket.
4. FUNCTION REQUIREMENTS — ADMINISTRATION SECTION (Admin)
Admin interface only for authorized admin/staff, UI: Dashboard + sidebar menu.
4.1. Authentication & Authorization
Admin login (2FA required).
Role management & granular rights (RBAC): SuperAdmin, ProductManager, OrderManager, Marketing, Support.
4.2. Product management
Product CRUD: name, slug, description, category, brand, attributes (size, color), variants (SKU), image (multi), price (original, sale), weight, dimensions, tag.
Integrated import/export (CSV/Excel).
Status management: Draft / Published / Archived.
SEO settings (meta title, meta desc, OG image).
4.3. Category & Brand Management
CRUD category (recursive), tags, brands.
4.4. Inventory Management
Inventory information by variant (warehouse if multi-warehouse).
Update import, export, transfer.
Low inventory warning (threshold), inventory report.
4.5. Order Management
View & filter orders by status, date, customer.
Change status, print invoice, create bill of lading, assign tracking number.
Process refunds, partial refunds, create credit notes.
Internal notes, upload documents (e.g. transfer files).
4.6. User Management
View user list, lock/unlock accounts, grant roles, view order & transaction history.
4.7. Promotion & Discount Code Management
Create coupons: % discount, fixed amount, free shipping, applicable conditions (by product/category/total amount), expiry date, number limit.
4.8. Content Management (CMS)
Static pages (About, Policy), banners, sliders, blog/news (optional).
Met management