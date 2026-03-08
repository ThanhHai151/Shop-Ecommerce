# Shop-Ecommerce

E-commerce website for computer parts built with Spring Boot and Thymeleaf.

## Yêu cầu

- Java 17+
- Docker & Docker Compose (chỉ để chạy SQL Server)
- Maven (hoặc dùng `./mvnw` đi kèm)

## Cấu trúc dự án

```
Shop-Ecommerce/
├── src/main/java/com/computershop/
│   ├── config/           # Configuration
│   ├── controller/       # Controllers
│   │   ├── web/          # User controllers
│   │   └── admin/        # Admin controllers
│   ├── dto/              # Data Transfer Objects
│   ├── exception/        # Exception handling
│   ├── main/             # Entities, Repositories
│   ├── service/          # Business logic
│   │   ├── interface/
│   │   └── impl/
│   └── util/             # Utilities
├── docker/               # Docker configs
└── src/main/resources/
    ├── application.properties
    └── application-distributed.properties
```

## Cách chạy (Development — Khuyến nghị)

Chạy theo 2 bước: **khởi động DB** → **khởi động app**.  
Không cần rebuild Docker mỗi lần sửa code.

### Bước 1 — Khởi động SQL Server (chỉ chạy 1 lần cho đến khi tắt máy)

```bash
docker compose -f docker/docker-compose.single.yml up mssql --detach
```

Kiểm tra DB đã sẵn sàng:

```bash
docker ps
# computershop-db   Up ... (healthy)   0.0.0.0:1433->1433/tcp
```

### Bước 2 — Chạy Spring Boot app (có Hot Reload)

```bash
./mvnw spring-boot:run
```

Truy cập: **http://localhost:2345**

> **Hot reload:** Khi sửa file Java, nhấn **Ctrl+S** — VS Code tự build, DevTools tự restart app (~1-2 giây).  
> Khi sửa HTML/CSS/JS, chỉ cần **refresh browser**, không cần restart.

### Chạy bằng VS Code (nhanh nhất)

1. Cài extension **"Extension Pack for Java"**
2. Nhấn **`F5`** (Debug) hoặc **`Ctrl+F5`** (Run)
3. Hoặc dùng task: **Ctrl+Shift+P** → `Tasks: Run Task` → chọn task

| Task                         | Mô tả                          |
| ---------------------------- | ------------------------------ |
| `Start DB (SQL Server only)` | Khởi động SQL Server container |
| `Stop DB`                    | Dừng SQL Server container      |
| `Build (skip tests)`         | Build nhanh không chạy test    |

### Kết nối DBeaver / SQL client

| Thông tin                | Giá trị               |
| ------------------------ | --------------------- |
| Host                     | `localhost`           |
| Port                     | `1433`                |
| Database                 | `computershop`        |
| Username                 | `sa`                  |
| Password                 | `YourStrong@Passw0rd` |
| Encrypt                  | `true`                |
| Trust Server Certificate | `true`                |

---

## Chạy toàn bộ bằng Docker (Production / Demo)

Chạy cả app lẫn DB trong Docker:

```bash
docker compose -f docker/docker-compose.single.yml up --build
```

Truy cập: **http://localhost:2345**

### Distributed mode (2 Databases)

```bash
docker compose -f docker/docker-compose.distributed.yml up --build
```

---

## Tài khoản mặc định

| Role  | Username | Password  |
| ----- | -------- | --------- |
| Admin | `admin`  | `123456`  |
| User  | `user`   | `user123` |
