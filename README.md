# English AI Coach

English AI Coach V1 dùng Spring Boot modular monolith làm backend authoritative cho các business rule. Mã nguồn backend nằm trong thư mục `backend/`.

## Backend build, test và run

Backend dùng:

- Java 21 LTS;
- Spring Boot 4.1.1;
- Maven 3.9.16 thông qua Maven Wrapper.

Yêu cầu máy phát triển có JDK 21 trở lên. Không cần cài Maven toàn cục vì wrapper sẽ tải đúng phiên bản Maven đã khóa. Các lệnh dưới đây chạy từ thư mục gốc của repository.

Build và test trên Windows:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml clean verify
```

Build và test trên Linux/macOS:

```bash
sh ./backend/mvnw -f backend/pom.xml clean verify
```

Lệnh `clean verify` xóa build cũ, biên dịch ứng dụng, chạy smoke test Spring context và đóng gói executable JAR tại `backend/target/english-ai-coach-backend-0.0.1-SNAPSHOT.jar`.

Sau khi build thành công, chạy backend trên Windows:

```powershell
java -jar backend\target\english-ai-coach-backend-0.0.1-SNAPSHOT.jar
```

Chạy backend trên Linux/macOS:

```bash
java -jar backend/target/english-ai-coach-backend-0.0.1-SNAPSHOT.jar
```

Dừng ứng dụng bằng `Ctrl+C`.

Task `BE-FND-001` chỉ bootstrap ứng dụng và smoke test. Package/module skeleton, application profiles, PostgreSQL, Flyway, security, Actuator và product API thuộc các backlog task kế tiếp.

## PostgreSQL local với Docker Compose

DB-FND-001 cung cấp PostgreSQL local độc lập để các task database/backend tiếp theo sử dụng. Yêu cầu Docker Engine hoặc Docker Desktop có Docker Compose.

Từ thư mục gốc repository, tạo file môi trường local trên Windows:

```powershell
Copy-Item .env.example .env
```

Trên Linux/macOS:

```bash
cp .env.example .env
```

Đổi `POSTGRES_PASSWORD` trong `.env` trước khi chạy. `.env` bị Git bỏ qua và không được commit; `.env.example` chỉ chứa placeholder, không phải credential thật.

Kiểm tra cấu hình rồi khởi động PostgreSQL và chờ healthcheck thành công:

```bash
docker compose config --quiet
docker compose up -d --wait postgres
```

Kiểm tra trạng thái và khả năng nhận kết nối:

```bash
docker compose ps postgres
docker compose exec -T postgres sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

PostgreSQL chỉ bind vào `127.0.0.1` theo mặc định. Host port lấy từ `POSTGRES_PORT`; database và user lấy từ `POSTGRES_DB` và `POSTGRES_USER` trong `.env`.

Dừng và khởi động lại cùng database volume:

```bash
docker compose stop postgres
docker compose up -d --wait postgres
```

Gỡ container/network nhưng giữ dữ liệu trong named volume:

```bash
docker compose down
```

Chỉ khi cần xóa toàn bộ dữ liệu local và khởi tạo lại từ đầu:

```bash
docker compose down --volumes
```

Lệnh cuối xóa named volume PostgreSQL của project và không thể khôi phục dữ liệu từ volume đó nếu chưa backup. DB-FND-001 không tạo schema/Flyway migration và không cấu hình backend datasource; các phần này thuộc task kế tiếp.
