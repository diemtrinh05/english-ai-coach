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
