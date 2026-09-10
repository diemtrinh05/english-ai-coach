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

Từ DB-FND-002, executable JAR cần PostgreSQL đang chạy và các biến `SPRING_DATASOURCE_*` được cấu hình như phần Flyway bên dưới; lệnh `java -jar` trần chỉ minh họa entry point của artifact.

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

Lệnh cuối xóa named volume PostgreSQL của project và không thể khôi phục dữ liệu từ volume đó nếu chưa backup.

## Flyway schema baseline

DB-FND-002 cung cấp migration `V1__create_schema_baseline.sql` tại đường dẫn chuẩn `backend/src/main/resources/db/migration/`. Đây là baseline khởi tạo mới gồm đúng 34 bảng theo Database Schema v1.6; sau khi migration được chia sẻ hoặc apply, không sửa file V1 mà phải thêm migration version mới.

Backend dùng Spring Boot Flyway starter và PostgreSQL driver. Sau khi PostgreSQL đã healthy, cấu hình datasource bằng biến môi trường rồi khởi động backend để Flyway tự migrate. Các giá trị phải khớp `.env`; ví dụ dưới đây dùng port/database/user mặc định của `.env.example` và yêu cầu thay placeholder password bằng giá trị local thực tế.

PowerShell từ thư mục gốc repository:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://127.0.0.1:5432/english_ai_coach"
$env:SPRING_DATASOURCE_USERNAME = "english_ai_coach"
$env:SPRING_DATASOURCE_PASSWORD = "<same-local-password-as-.env>"
java -jar backend\target\english-ai-coach-backend-0.0.1-SNAPSHOT.jar
```

Trên Linux/macOS:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/english_ai_coach"
export SPRING_DATASOURCE_USERNAME="english_ai_coach"
export SPRING_DATASOURCE_PASSWORD="<same-local-password-as-.env>"
java -jar backend/target/english-ai-coach-backend-0.0.1-SNAPSHOT.jar
```

Flyway ghi version đã apply vào `flyway_schema_history`. Việc seed reference data, bổ sung performance/partial indexes, repository và Testcontainers harness thuộc các backlog task kế tiếp.

## JPA base conventions

BE-FND-004 bổ sung Spring Data JPA/Hibernate và các convention nền tảng sau:

- ID nghiệp vụ do application sinh bằng UUID;
- cột `TIMESTAMPTZ` được map bằng `Instant` để giữ một instant UTC rõ ràng;
- cột ngày không kèm timezone như `last_active_date` được map bằng `LocalDate`;
- enum nghiệp vụ được lưu bằng `EnumType.STRING`, không dùng ordinal;
- `user_vocabulary_progress.version` và `streaks.version` dùng JPA `@Version`.

`UserVocabularyProgress` và `Streak` là hai mapping nền tảng được thêm trong task này vì Database Schema v1.6 yêu cầu optimistic locking cho đúng hai bảng đó. Repository, transaction service, HTTP 409 `CONCURRENT_UPDATE`, business mutation và Testcontainers harness vẫn thuộc các task phụ thuộc tiếp theo.

Có thể kiểm tra Flyway và mapping Hibernate trên PostgreSQL đang chạy bằng cách thêm tùy chọn sau khi khởi động JAR:

```text
--spring.jpa.hibernate.ddl-auto=validate
```

Tùy chọn này chỉ validate mapping với schema hiện hữu; Flyway vẫn là cơ chế duy nhất quản lý thay đổi schema.

## PostgreSQL integration tests

QA-FND-001 cung cấp test harness dùng Testcontainers với đúng PostgreSQL image của môi trường local. Mỗi Spring Boot integration-test context có thể kế thừa `PostgreSqlIntegrationTestSupport` để nhận datasource cô lập; Flyway tự migrate database mới và Hibernate validate các JPA mapping hiện có.

Máy chạy test cần Docker Engine tương thích với Testcontainers. Chạy toàn bộ unit và integration tests từ thư mục gốc repository:

```powershell
.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
```

Suite nền tảng xác minh database thực sự là PostgreSQL, Flyway tạo đủ 34 bảng canonical, và PostgreSQL xử lý trực tiếp `JSONB`, `TIMESTAMPTZ` cùng `CHECK` constraint. H2 không được cấu hình hoặc dùng làm bằng chứng thay thế cho các behavior này.
