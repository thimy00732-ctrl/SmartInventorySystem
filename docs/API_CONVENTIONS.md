# Smart Inventory System — API Contract

**Phiên bản:** 1.0  
**Trạng thái:** Bản dự thảo chờ nhóm thống nhất  
**Backend:** Kotlin + Ktor  
**Database:** MySQL  
**Client:** Android Kotlin  
**Định dạng dữ liệu:** JSON

---

## 1. Mục đích

Tài liệu này thống nhất cách Android giao tiếp với Backend của Smart Inventory System.

Tất cả thành viên phải tuân thủ các quy ước về endpoint, HTTP method, tên trường JSON, mã trạng thái HTTP, định dạng phản hồi và cơ chế xác thực.

Các endpoint nghiệp vụ trong tài liệu là đề xuất ban đầu, chưa được xác nhận là đã triển khai. Chỉ đánh dấu hoàn thành sau khi API được code và kiểm thử thực tế.

## 2. Trạng thái hiện tại

| Endpoint | Chức năng | Trạng thái |
|---|---|---|
| `GET /health` | Kiểm tra Backend | Đã kiểm thử |
| `GET /health/database` | Kiểm tra kết nối MySQL | Đã kiểm thử thành công |
| `/api/v1/...` | Các API nghiệp vụ | Chưa xác nhận triển khai |

## 3. Quy ước chung

### 3.1. Base URL

Trong môi trường phát triển:

- Backend chạy trên máy tính: `http://localhost:8080`
- Android Emulator truy cập máy tính phát triển: `http://10.0.2.2:8080`

`10.0.2.2` dành cho Android Emulator. Nếu chạy trên điện thoại thật, cần sử dụng địa chỉ IP LAN của máy tính và cấu hình mạng phù hợp.

Các endpoint nghiệp vụ dự kiến sử dụng tiền tố `/api/v1`.

### 3.2. HTTP methods

| Method | Mục đích |
|---|---|
| GET | Lấy dữ liệu |
| POST | Tạo tài nguyên hoặc thực hiện thao tác |
| PUT | Thay thế/cập nhật toàn bộ tài nguyên |
| PATCH | Cập nhật một phần tài nguyên |
| DELETE | Xóa tài nguyên |

### 3.3. Quy tắc đặt tên

- Endpoint sử dụng danh từ số nhiều khi truy cập danh sách tài nguyên, ví dụ `/products`, `/warehouses`.
- Tên trường JSON sử dụng `camelCase`, ví dụ `productId`, `minStock`.
- Tên bảng và cột trong MySQL tuân theo thiết kế cơ sở dữ liệu đã thống nhất.
- Request và response sử dụng JSON, trừ khi có quy định riêng.

### 3.4. Header

Request JSON thông thường:

`Content-Type: application/json`

Các API yêu cầu đăng nhập sử dụng:

`Authorization: Bearer <accessToken>`

API đăng nhập và các API công khai không bắt buộc JWT, trừ khi nhóm có quy định khác.

## 4. Quy ước response

### 4.1. Response thành công

Đề xuất cấu trúc chung:

```json
{
  "success": true,
  "message": "Request successful",
  "data": {}
}
```

Đối với API trả về danh sách, `data` là mảng. Đối với API không có dữ liệu trả về, nhóm cần thống nhất cách xử lý; có thể dùng HTTP 204 và không trả body.

### 4.2. Response lỗi

Đề xuất cấu trúc chung:

```json
{
  "success": false,
  "message": "Request failed",
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND"
  }
}
```

Các trường lỗi có thể được mở rộng khi cần. Backend không trả về mật khẩu, secret, stack trace hoặc thông tin kết nối cơ sở dữ liệu cho client.

### 4.3. HTTP status codes

| Status | Ý nghĩa |
|---|---|
| 200 | Thành công |
| 201 | Tạo tài nguyên thành công |
| 204 | Thành công, không có response body |
| 400 | Request không hợp lệ |
| 401 | Chưa xác thực hoặc token không hợp lệ |
| 403 | Không đủ quyền |
| 404 | Không tìm thấy tài nguyên |
| 409 | Xung đột dữ liệu |
| 500 | Lỗi máy chủ |

Các status này là quy ước đề xuất, cần áp dụng thống nhất trong Backend.

## 5. Authentication và phân quyền

Các endpoint dự kiến:

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/api/v1/auth/login` | Đăng nhập |
| GET | `/api/v1/auth/me` | Lấy thông tin tài khoản hiện tại |
| POST | `/api/v1/auth/logout` | Đăng xuất nếu cơ chế token hỗ trợ |

Request đăng nhập đề xuất:

```json
{
  "username": "staff01",
  "password": "example-password"
}
```

Response đăng nhập đề xuất:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<jwt-token>",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "username": "staff01",
      "fullName": "Nguyen Van A",
      "role": "STAFF"
    }
  }
}
```

Quy ước bảo mật:

- Không lưu mật khẩu dạng văn bản thuần.
- Backend xác minh mật khẩu bằng cơ chế hash an toàn.
- Các API riêng tư phải kiểm tra JWT.
- Admin và Staff chỉ được thực hiện các chức năng phù hợp với quyền của mình.
- Cần thống nhất thời hạn token và cách xử lý token hết hạn.

## 6. Products API

Các endpoint đề xuất:

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/v1/products` | Danh sách và tìm kiếm sản phẩm |
| GET | `/api/v1/products/{id}` | Chi tiết sản phẩm |
| POST | `/api/v1/products` | Tạo sản phẩm |
| PUT | `/api/v1/products/{id}` | Cập nhật sản phẩm |
| DELETE | `/api/v1/products/{id}` | Xóa sản phẩm |

Query parameters đề xuất cho danh sách:

- `keyword`: tìm theo tên, SKU hoặc mã sản phẩm.
- `page`: số trang.
- `size`: số bản ghi mỗi trang.

Ví dụ response sản phẩm:

```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "sku": "SP001",
    "name": "Fresh Milk",
    "categoryId": 1,
    "price": 30000,
    "minStock": 10
  }
}
```

Tên trường và kiểu dữ liệu cuối cùng phải khớp với database và được các thành viên xác nhận.

## 7. Warehouses API

Các endpoint đề xuất:

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/v1/warehouses` | Danh sách kho |
| GET | `/api/v1/warehouses/{id}` | Chi tiết kho |
| POST | `/api/v1/warehouses` | Tạo kho |
| PUT | `/api/v1/warehouses/{id}` | Cập nhật kho |

Các trường như mã kho, tên kho, địa chỉ và trạng thái phải được thống nhất dựa trên bảng warehouse thực tế.

## 8. Inventory API

Các endpoint đề xuất:

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/v1/inventory` | Xem tồn kho |
| GET | `/api/v1/inventory/low-stock` | Danh sách sản phẩm tồn thấp |
| GET | `/api/v1/warehouses/{id}/inventory` | Tồn kho tại một kho |

Quy tắc nghiệp vụ:

- Tồn kho thực tế được đọc từ MySQL.
- Số lượng tồn phải được xác định theo sản phẩm và kho.
- `minStock` là ngưỡng tối thiểu dùng chung cho mỗi sản phẩm trên toàn hệ thống, không phải ngưỡng riêng theo kho.
- Cảnh báo tồn thấp được xác định theo quy tắc nhóm thống nhất, ví dụ so sánh lượng tồn với `minStock`.
- Không sử dụng AI để tự đoán số lượng tồn kho.

## 9. Transactions API

Các endpoint đề xuất:

| Method | Endpoint | Mục đích |
|---|---|---|
| POST | `/api/v1/transactions` | Tạo giao dịch kho |
| GET | `/api/v1/transactions` | Danh sách lịch sử giao dịch |
| GET | `/api/v1/transactions/{id}` | Chi tiết giao dịch |

Nhóm cần thống nhất:

- Loại giao dịch: nhập kho, xuất kho hoặc điều chỉnh.
- Kho liên quan, người thực hiện và thời gian.
- Danh sách sản phẩm cùng số lượng.
- Quy tắc không cho xuất vượt số lượng tồn thực tế.
- Cập nhật tồn kho và ghi lịch sử giao dịch trong cùng một transaction của database.
- Cách xử lý khi một giao dịch thất bại.

Cấu trúc request chi tiết sẽ được chốt sau khi đối chiếu các bảng transaction và transaction detail trong database.

## 10. AI Assistant API

Endpoint đề xuất:

`POST /api/v1/ai/chat`

Request dự kiến:

```json
{
  "message": "Sản phẩm nào đang có lượng tồn thấp?"
}
```

Cấu trúc response cần được thống nhất giữa Backend và thành viên phụ trách AI/RAG.

Quy tắc:

- AI chỉ trả lời thông tin tồn kho dựa trên dữ liệu được Backend truy xuất.
- Không cung cấp dữ liệu tài khoản hoặc thông tin nhạy cảm trái quyền truy cập.
- Phải xử lý lỗi từ dịch vụ AI và giới hạn thời gian chờ.
- Không coi endpoint này là đã triển khai cho đến khi được kiểm thử.

## 11. Validation và xử lý lỗi

Backend phải kiểm tra dữ liệu đầu vào trước khi truy vấn hoặc cập nhật database.

Ví dụ:

- Không chấp nhận số lượng âm nếu nghiệp vụ không cho phép.
- Không chấp nhận giá trị bắt buộc bị thiếu.
- Kiểm tra SKU hoặc mã sản phẩm trùng lặp.
- Kiểm tra sự tồn tại của sản phẩm và kho được tham chiếu.
- Không tiết lộ thông tin bí mật trong response lỗi.

Mỗi lỗi nghiệp vụ nên có mã lỗi ổn định để Android có thể xử lý nhất quán.

## 12. Quy trình thống nhất API Contract

1. Thành viên Backend đề xuất endpoint và cấu trúc dữ liệu.
2. Thành viên Android kiểm tra xem có đủ dữ liệu để xây dựng giao diện không.
3. Thành viên phụ trách chức năng kiểm tra các quy tắc nghiệp vụ.
4. Cả nhóm xác nhận các endpoint, request, response và quyền truy cập.
5. Cập nhật tài liệu này trước khi triển khai hoặc thay đổi API.
6. Khi API thay đổi, phải cập nhật tài liệu và thông báo cho các thành viên liên quan.
7. Chỉ đánh dấu API hoàn thành sau khi code và kiểm thử thực tế.

**Lưu ý:** Đây là bản dự thảo API Contract. Các endpoint nghiệp vụ phải được đối chiếu với database và mã nguồn hiện có trước khi chốt chính thức.
