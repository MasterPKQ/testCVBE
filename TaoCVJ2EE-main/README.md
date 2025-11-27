# TaoCV Application

## 🚀 Quick Start với Docker

### Bước 1: Cấu hình GOOGLE_API_KEY (BẮT BUỘC)

**Tùy chọn 1: Tạo file .env (KHUYẾN NGHỊ)**

Tạo file `.env` trong thư mục gốc `D:\ProjectJ2EE\TaoCV\`:

```bash
# Windows PowerShell hoặc Git Bash
cp .env.example .env

# Windows CMD
copy .env.example .env
```

Mở file `.env` và **THAY ĐỔI** dòng này:
```env
GOOGLE_API_KEY=AIzaSyBQaav4A6h7OfdVznvUT_Uh6hTx0MhOm-g
```

Thành API key thật của bạn (lấy từ https://aistudio.google.com/app/apikey):
```env
GOOGLE_API_KEY=AIzaSy_YOUR_ACTUAL_KEY_HERE
```

**Tùy chọn 2: Set biến môi trường trực tiếp**

Windows PowerShell:
```powershell
$env:GOOGLE_API_KEY="AIzaSy_YOUR_ACTUAL_KEY_HERE"
docker-compose up -d --build
```

Windows CMD:
```cmd
set GOOGLE_API_KEY=AIzaSy_YOUR_ACTUAL_KEY_HERE
docker-compose up -d --build
```

Linux/Mac:
```bash
export GOOGLE_API_KEY=AIzaSy_YOUR_ACTUAL_KEY_HERE
docker-compose up -d --build
```

### Bước 2: Lấy Google API Key

1. Truy cập: https://aistudio.google.com/app/apikey
2. Click "Create API Key"
3. Copy API key (bắt đầu với `AIzaSy...`)
4. Paste vào file `.env`

### Bước 3: Chạy Docker

```bash
# Build và start tất cả services
docker-compose up -d --build

# Xem logs để kiểm tra
docker-compose logs -f ml-worker

# Nếu thấy "✓ GOOGLE_API_KEY loaded successfully" => OK!
```

### Kiểm tra services

- **Spring API**: http://localhost:8080/api
- **ML Worker**: http://localhost:8000
- **ML Worker Docs**: http://localhost:8000/docs
- **Health Check**: http://localhost:8000/health
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

### 🛠️ Troubleshooting

**❌ Lỗi: "GOOGLE_API_KEY not found in environment variables"**

**Giải pháp:**
1. Kiểm tra file `.env` có tồn tại trong `D:\ProjectJ2EE\TaoCV\` không
2. Mở file `.env` và đảm bảo có dòng: `GOOGLE_API_KEY=AIzaSy...`
3. API key phải bắt đầu bằng `AIzaSy` và không có khoảng trắng
4. Restart container:
   ```bash
   docker-compose restart ml-worker
   docker-compose logs -f ml-worker
   ```

**❌ Lỗi: "No such image: taocv-spring-app:latest"**

**Giải pháp:**
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

**❌ Warning: "GOOGLE_API_KEY variable is not set"**

**Giải pháp:**
- Bạn đang chạy `docker-compose up` mà chưa có file `.env`
- Tạo file `.env` theo hướng dẫn ở Bước 1

**❌ Container ml-worker liên tục restart**

**Giải pháp:**
```bash
# Xem log chi tiết
docker-compose logs ml-worker

# Nếu thấy "GOOGLE_API_KEY not found" => Tạo file .env
# Nếu thấy lỗi khác => Copy lỗi và hỏi
```

### 🧹 Clean Up

```bash
# Stop tất cả services
docker-compose down

# Stop và xóa volumes (xóa database)
docker-compose down -v

# Xóa tất cả images cũ
docker system prune -a
```

## 💻 Development (Local - không dùng Docker)

### 1. Start MySQL và Redis

```bash
docker-compose up mysql redis -d
```

### 2. Tạo file .env cho ML Worker

```bash
cd ml-worker
cp ../.env.example .env
# Thêm GOOGLE_API_KEY vào file .env
```

### 3. Start Spring Boot

```bash
./mvnw spring-boot:run
```

### 4. Start ML Worker

```bash
cd ml-worker
pip install -r requirements.txt
python -m spacy download en_core_web_sm
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

## 📝 Notes

- File `.env` đã được thêm vào `.gitignore` để bảo mật
- **KHÔNG COMMIT** file `.env` lên Git
- Mỗi developer cần tạo file `.env` riêng với API key của mình
- API key trong `.env.example` chỉ là ví dụ, cần thay bằng key thật
