# Shop-Ecommerce

E-commerce website for computer parts built with Spring Boot and Thymeleaf.

## Cấu trúc dự án

```
Shop-Ecommerce/
├── src/main/java/com/computershop/
│   ├── config/           # Configuration
│   ├── controller/      # Controllers
│   │   ├── web/        # User controllers
│   │   └── admin/      # Admin controllers
│   ├── dto/            # Data Transfer Objects
│   ├── exception/      # Exception handling
│   ├── main/           # Entities, Repositories
│   ├── service/        # Business logic
│   │   ├── interface/
│   │   └── impl/
│   └── util/           # Utilities
├── docker/             # Docker configs
└── src/main/resources/
```

## Cách chạy

### Chạy cục bộ (Single Database)

```bash
mvn spring-boot:run
```

Truy cập: http://localhost:2345

### Chạy với Docker (Single DB)

```bash
cd docker
docker-compose -f docker-compose.single.yml up --build
```

### Chạy Distributed (2 Databases)

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=distributed
```

### Docker Distributed

```bash
cd docker
docker-compose -f docker-compose.distributed.yml up --build
```

## Tài khoản mặc định

- Admin: admin / admin123
- User: user / user123
