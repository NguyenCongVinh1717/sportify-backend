# 🏆 Sportify Backend - E-Commerce API & AI Integration

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-brightgreen?style=for-the-badge&logo=springboot)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0-blue?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache-red?style=for-the-badge&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Supported-blue?style=for-the-badge&logo=docker)

Hệ thống RESTful API Backend chuyên biệt cho nền tảng thương mại điện tử **Sportify** (kinh doanh thời trang & phụ kiện thể thao). Dự án được tích hợp các công nghệ tiên tiến nhất như **Spring AI (RAG với Google Gemini)**, **Tìm kiếm sản phẩm bằng hình ảnh (Vector Search / pgvector)**, **Thanh toán trực tuyến VNPay**, **Redis Caching**, và **Xác thực JWT / Google OAuth2**.

---

## ✨ Tính Năng Nổi Bật

### 🤖 1. Tích Hợp AI Trí Tuệ Nhân Tạo (Spring AI & Vector Search)
- **Tư vấn bán hàng tự động (AI Chatbot RAG)**: Sử dụng Google Gemini 2.5 Flash kết hợp với kĩ thuật **RAG (Retrieval-Augmented Generation)** để tra cứu và tư vấn thông tin sản phẩm chuẩn xác cho khách hàng dựa trên dữ liệu sản phẩm lưu trong PostgreSQL (`pgvector`).
- **Tìm kiếm sản phẩm bằng hình ảnh (Visual Search)**: Cho phép người dùng tải lên hình ảnh sản phẩm để tìm kiếm các sản phẩm tương tự trong cửa hàng thông qua vector embedding của hình ảnh.

### 🔐 2. Xác Thực & Phân Quyền (Authentication & Authorization)
- Đăng ký & Đăng nhập bằng Email/Password kèm xác thực OTP qua Email (SendGrid / SMTP).
- Đăng nhập nhanh với **Google OAuth2**.
- Bảo mật với **JWT (JSON Web Token)** & Refresh Token lưu trữ an toàn trong HttpOnly Cookie (`SameSite=None`, `Secure`).
- Phân quyền người dùng (User / Admin).

### 🛍️ 3. Quản Lý Sản Phẩm & Danh Mục (Catalog Management)
- Quản lý Thương hiệu (Brand), Kích thước (Size), Màu sắc (Color), Hình ảnh sản phẩm (Cloudinary & Local Storage).
- Biến thể sản phẩm (ProductColorSize) với quản lý số lượng tồn kho riêng biệt.
- Tích hợp **Redis Cache** giúp tối ưu tốc độ truy vấn danh mục và thông tin sản phẩm.

### 🛒 4. Giỏ Hàng & Danh Sách Yêu Thích (Cart & Wishlist)
- Thêm, sửa, xóa sản phẩm trong giỏ hàng.
- Lưu trữ danh sách sản phẩm yêu thích (Wishlist) của từng người dùng.

### 💳 5. Đơn Hàng & Thanh Toán Trực Tuyến (Order & VNPay Integration)
- Đặt hàng và tự động tính toán tổng tiền dựa trên giá trị thực tế sản phẩm.
- Tích hợp cổng thanh toán trực tuyến **VNPay Sandbox** với cơ chế xử lý callback an toàn.
- Admin Dashboard quản lý đơn hàng: Cập nhật trạng thái đơn hàng (CHỜ XỬ LÝ, ĐÃ XÁC NHẬN, ĐANG GIAO, HOÀN THÀNH, HỦY).

---

## 🛠️ Công Nghệ Sử Dụng

- **Core & Framework**: Java 21, Spring Boot 4.0.4
- **Security**: Spring Security, JJWT (io.jsonwebtoken 0.12.5), Google API Client
- **Database**: PostgreSQL (kèm extension `pgvector`), Spring Data JPA, Hibernate
- **Caching**: Spring Data Redis, Caffeine Cache
- **AI & Vector Engine**: Spring AI 2.0.0 GA, Google GenAI (Gemini 2.5 Flash, Gemini Embedding), `spring-ai-pgvector-store`
- **Media & File Storage**: Cloudinary SDK, Local Upload
- **Email Service**: Spring Boot Starter Mail, SendGrid API
- **Payment Gateway**: VNPay Integration
- **Build & Tools**: Apache Maven, Lombok, JaCoCo (Test Coverage Metrics)
- **Containerization**: Docker (Multi-stage build)

---

## 📁 Cấu Trúc Thư Mục Dự Án

```
VinhNguyen/
├── src/main/java/SaleManagement/VinhNguyen/
│   ├── config/             # Cấu hình Spring AI, Redis, Cloudinary, Web CORS, Security...
│   ├── configuration/      # Các file cấu hình mở rộng
│   ├── controller/         # REST Controllers xử lý API Endpoints
│   │   ├── AdminOrderController.java
│   │   ├── AiConsultController.java
│   │   ├── AuthController.java
│   │   ├── BrandController.java
│   │   ├── CartController.java
│   │   ├── ColorController.java
│   │   ├── CommentController.java
│   │   ├── ImageSearchController.java
│   │   ├── OrderController.java
│   │   ├── ProductController.java
│   │   ├── SizeController.java
│   │   ├── UploadController.java
│   │   ├── UserController.java
│   │   └── WishlistController.java
│   ├── entity/             # JPA Entities (User, Product, Order, Cart, Brand...)
│   ├── enums/              # Enum định nghĩa Trạng thái, Roles...
│   ├── exception/          # Xử lý ngoại lệ tập trung (Global Exception Handler)
│   ├── mapper/             # MapStruct / DTO Mappers
│   ├── repository/         # Spring Data JPA Repositories
│   ├── request/            # Data Transfer Objects (DTO) cho Request
│   ├── response/           # Data Transfer Objects (DTO) cho Response
│   ├── security/           # Custom JWT Filter, UserDetailsService
│   └── service/            # Business Logic Services
├── src/main/resources/
│   ├── application.properties         # File cấu hình chính
│   └── application.properties.example # File mẫu cấu hình biến môi trường
├── Dockerfile              # Dockerfile build và chạy app
├── pom.xml                 # Cấu hình Maven & dependencies
└── README.md
```

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy

### 📋 1. Yêu Cầu Tiền Đề (Prerequisites)
- **Java Development Kit (JDK)**: phiên bản 21 trở lên
- **Maven**: phiên bản 3.9 trở lên (hoặc dùng `mvnw` đi kèm dự án)
- **PostgreSQL**: Đã bật extension `pgvector`
- **Redis Server**: Đang chạy trên cổng `6379`
- **Google Gemini API Key**: Lấy từ Google AI Studio
- *(Tùy chọn)* **Python Vector Service**: Đang chạy nếu sử dụng tính năng trích xuất vector ảnh nâng cao.

### ⚙️ 2. Cấu Hình Biến Môi Trường (Environment Variables)

Tạo file `src/main/resources/application.properties` hoặc thiết lập các biến môi trường hệ thống theo mẫu dưới đây:

```properties
server.port=8081

# Database PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sportify_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# JWT Security Key
JWT_SECRET=your_super_secret_jwt_key_at_least_256_bits_long

# Mail Service (SendGrid / SMTP)
SPRING_MAIL_USERNAME=apikey
SPRING_MAIL_PASSWORD=your_sendgrid_api_key

# Google Gemini AI Config
SPRING_AI_GOOGLE_GENAI_API_KEY=your_google_gemini_api_key

# Cloudinary Config
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Redis Config
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Python AI Image Vector Service (Tùy chọn)
PYTHON_AI_SERVICE_URL=http://127.0.0.1:8000/extract-image-vector
```

### 🏃 3. Khởi Chạy Ứng Dụng (Local Development)

#### Bằng Maven Wrapper:
```bash
# Trên Windows PowerShell
.\mvnw.cmd spring-boot:run

# Trên Linux/macOS
./mvnw spring-boot:run
```

Ứng dụng sẽ khởi chạy tại giao thức HTTP trên cổng **8081**: `http://localhost:8081`

---

## 🐳 Khởi Chạy Với Docker

Dự án hỗ trợ Multi-stage Docker build để tối ưu dung lượng image và bảo mật:

```bash
# 1. Build Docker Image
docker build -t sportify-backend .

# 2. Khởi chạy Container
docker run -d -p 8081:8081 \
  --name sportify-backend-container \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/sportify_db" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="your_password" \
  -e JWT_SECRET="your_jwt_secret" \
  -e SPRING_AI_GOOGLE_GENAI_API_KEY="your_gemini_api_key" \
  sportify-backend
```

---

## 📌 Danh Sách API Điển Hình (API Endpoints Overview)

### 🔐 Auth & Người dùng
| HTTP Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/auth/register` | Đăng ký tài khoản mới |
| `POST` | `/auth/login` | Đăng nhập hệ thống (trả về Access Token & Cookie Refresh Token) |
| `POST` | `/auth/google` | Đăng nhập bằng Google ID Token |
| `POST` | `/auth/verify-otp` | Xác thực mã OTP gửi về Email |
| `POST` | `/auth/refresh` | Làm mới Access Token bằng Refresh Token trong Cookie |
| `POST` | `/auth/logout` | Đăng xuất & xóa Cookie Refresh Token |
| `GET` | `/users/me` | Lấy thông tin cá nhân của người dùng hiện tại |

### 👟 Sản phẩm & Danh mục
| HTTP Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/products` | Lấy danh sách sản phẩm (có lọc, phân trang, caching) |
| `GET` | `/products/{id}` | Lấy chi tiết thông tin sản phẩm |
| `POST` | `/products` | Tạo sản phẩm mới (*Admin*) |
| `PUT` | `/products/{id}` | Cập nhật thông tin sản phẩm (*Admin*) |
| `DELETE` | `/products/{id}` | Xóa sản phẩm (*Admin*) |

### 🤖 AI Consult & Image Search
| HTTP Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/ai/consult` | Gửi câu hỏi tư vấn cho AI Assistant (Spring AI RAG + Gemini) |
| `POST` | `/ai/reindex` | Lập lại chỉ mục vector toàn bộ sản phẩm vào Postgres |
| `POST` | `/ai/image/search` | Tìm kiếm sản phẩm tương tự bằng cách tải lên 1 hình ảnh |
| `POST` | `/ai/image/index` | Nạp vector hình ảnh cho 1 sản phẩm |
| `POST` | `/ai/image/reindex-all` | Nạp lại vector hình ảnh cho toàn bộ sản phẩm |

### 🛒 Giỏ hàng, Đơn hàng & Thanh toán
| HTTP Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/cart` | Xem giỏ hàng của người dùng hiện tại |
| `POST` | `/cart/add` | Thêm sản phẩm vào giỏ hàng |
| `POST` | `/order/create` | Tạo đơn hàng và lấy link thanh toán VNPay |
| `GET` | `/order/vnpay-callback` | Callback URL xử lý phản hồi kết quả thanh toán từ VNPay |
| `GET` | `/admin/orders` | Quản lý toàn bộ đơn hàng (*Admin*) |

---

## 🧪 Kiểm Thử (Testing & Coverage)

Chạy bộ kiểm thử tự động và xuất báo cáo độ phủ mã nguồn (JaCoCo report):

```bash
mvn clean test
```
Báo cáo JaCoCo sẽ được tạo tại đường dẫn: `target/site/jacoco/index.html`.

---

## 📝 Đóng Góp & Bản Quyền

Dự án được phát triển bởi **NguyenCongVinh1717** phục vụ hệ sinh thái thương mại điện tử **Sportify**.
