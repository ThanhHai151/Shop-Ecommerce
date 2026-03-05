## Run ComputerShop (JSB-ComputerShop)

### 1. Prerequisites (chạy trực tiếp)
- **Java**: JDK 17 (or higher) installed and on your PATH (`java -version`).
- **Database**: SQL Server running on `localhost:1433` with a database named `computershop`.
  - Either create user **`hai` / `hai`** with access to `computershop`,
  - **Hoặc** sửa file `src/main/resources/application.properties` để dùng tài khoản thực tế của bạn.

### 2. Chạy ứng dụng bằng Maven wrapper (không cần cài Maven)
Tại thư mục gốc project:

```bash
cd "D:\New folder\ComputerShop\JSB-ComputerShop"
.\mvnw.cmd spring-boot:run
```

Đợi tới khi log hiển thị tương tự:

```text
Tomcat initialized with port 2345 (http)
Started MainApplication in ... seconds
```

Sau đó mở trình duyệt và truy cập:

```text
http://localhost:2345
```

### 3. Chạy bằng Docker (tự động kèm SQL Server, KHÔNG cần cài SQL Server)
- Cài **Docker Desktop** cho Windows (bật chế độ Linux containers).
- Tại thư mục gốc project (nơi có file `docker-compose.yml`):

```bash
cd "D:\New folder\ComputerShop\JSB-ComputerShop"
docker compose up --build
```

- Đợi lần chạy đầu tiên (build image + khởi tạo database) có thể mất vài phút.
- Sau khi log cho thấy ứng dụng đã start, truy cập: `http://localhost:2345`.
- Dừng toàn bộ container:

```bash
docker compose down
```
\mvnw.cmd spring-boot:run


account 
user@computershop.com / user123
admin@computershop.com / admin123admin123