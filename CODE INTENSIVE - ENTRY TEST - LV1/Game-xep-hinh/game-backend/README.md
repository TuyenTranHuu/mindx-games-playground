# Puzzle Game API

Backend cho Game xếp hình, sử dụng Java 21, Spring Boot 3.5 và PostgreSQL.

## Chạy local

1. Cài Java 21 và PostgreSQL. Nếu có Docker, chạy `docker compose up -d` để tạo PostgreSQL.
2. Khai báo các biến môi trường dựa trên `.env.example`.
3. Trên Windows, chạy `mvnw.cmd spring-boot:run`.
4. Kiểm tra `http://localhost:8080/actuator/health`.

## Kiểm thử

Trên Windows, chạy `mvnw.cmd test`. Maven Wrapper sẽ tự tải Maven nên không cần cài Maven riêng.

## API đã có trong giai đoạn 1–4

- `POST /api/players/anonymous`
- `POST /api/players/token`
- `POST /api/players/recover`
- `GET/PATCH /api/players/me`
- `POST /api/games`
- `POST /api/games/{id}/finish`
- `GET /api/games/history`

Không commit mật khẩu Database, JWT secret, HMAC secret hoặc file `.env`.
