# Tài liệu phân quyền – Hệ thống quản lý kho

**Vai trò**: ADMIN và WAREHOUSE_STAFF  
**Căn cứ**: Đặc tả 111 chức năng UC01–UC111, database warehouse_db

---

## 1. Mục đích và phạm vi

Tài liệu quy định mỗi vai trò được dùng chức năng nào trong 111 UC của hệ thống, để làm căn cứ cho ba việc: **kiểm tra quyền ở backend Ktor** (UC04, UC07), **ẩn hiện mục menu** trên ứng dụng Android, và **viết test phân quyền**.

Phần nào đặc tả gốc đã nêu rõ (ví dụ chỉ người có quyền mới được duyệt phiếu) thì tài liệu bám theo. Phần nào đặc tả để ngỏ thì tài liệu đưa ra đề xuất và liệt kê ở **mục 8** để bạn xác nhận.

---

## 2. Hai vai trò

| Vai trò | Giá trị trong cột role | Mô tả |
|---------|------------------------|--------|
| **Quản trị viên** | `ADMIN` | Quản lý tài khoản, dữ liệu danh mục, phê duyệt phiếu, xác nhận kiểm kê, điều chỉnh tồn kho, xem toàn bộ số liệu tài chính. |
| **Nhân viên kho** | `WAREHOUSE_STAFF` | Làm nghiệp vụ hằng ngày: lập phiếu nhập, xuất, kiểm kê, quét mã, tra cứu tồn kho và lô hàng, xem báo cáo vận hành. |

> **Lưu ý**: Đặc tả ghi chú schema hiện chỉ có hai vai trò này. Nếu sau này cần vai trò `MANAGER` (quản lý kho chuyên duyệt phiếu), phải mở rộng ENUM và bảng quyền dưới đây.

---

## 3. Nguyên tắc phân quyền

1. **Mặc định từ chối**: Chức năng nào không được liệt kê cho vai trò thì bị chặn.

2. **Backend là nơi quyết định quyền**. Ứng dụng chỉ ẩn nút và menu cho gọn, không dùng việc ẩn UI làm biện pháp bảo vệ.

3. **Nhân viên kho lập phiếu, Admin duyệt phiếu**. Nhân viên không tự duyệt phiếu của mình.

4. **Dữ liệu danh mục** (sản phẩm, danh mục, nhà cung cấp) và thiết lập ngưỡng do Admin quản lý. Nhân viên chỉ xem, tìm, lọc.

5. Các thao tác **làm thay đổi tồn kho trực tiếp** mà không qua phiếu nhập hoặc xuất (phiếu điều chỉnh, cập nhật tồn sau kiểm kê) chỉ Admin được làm.

6. **Giá trị tổng hợp theo giá vốn** (UC39, UC47, UC91) chỉ Admin xem.

7. Tài khoản không ở trạng thái `ACTIVE` hoặc token đã bị thu hồi thì bị từ chối ở mọi chức năng (UC01, UC02, UC07).

---

## 4. Cách đọc ma trận

| Ký hiệu | Ý nghĩa |
|---------|---------|
| **✓** | Được thực hiện đầy đủ |
| **✗** | Không được thực hiện |
| **Giới hạn** | Được thực hiện nhưng có điều kiện, xem cột ghi chú |
| **Tự động** | Hệ thống tự thực hiện hoặc tự kiểm tra, người dùng không thao tác trực tiếp |

---

## 5. Ma trận phân quyền chi tiết

### I. Phân quyền và quản lý người dùng

| UC | Chức năng | Admin | Nhân viên | Ghi chú |
|----|-----------|-------|-----------|---------|
| UC01 | Đăng nhập | ✓ | ✓ | |
| UC02 | Đăng xuất | ✓ | ✓ | |
| UC03 | Quản lý tài khoản | ✓ | ✗ | Chỉ Admin tạo, sửa, khóa tài khoản |
| UC04 | Phân quyền người dùng | ✓ | ✗ | Chỉ Admin gán vai trò |
| UC05 | Quản lý thông tin cá nhân | ✓ | Giới hạn | Chỉ hồ sơ của chính mình |
| UC06 | Đổi mật khẩu | ✓ | Giới hạn | Chỉ mật khẩu của chính mình |
| UC07 | Kiểm soát truy cập | Tự động | Tự động | Kiểm tra trạng thái, vai trò, token ở mọi API |

---

### II. Quản lý danh mục và sản phẩm

| UC | Chức năng | Admin | Nhân viên | Ghi chú |
|----|-----------|-------|-----------|---------|
| UC08 | Thêm sản phẩm | ✓ | ✗ | |
| UC09 | Xem danh sách sản phẩm | ✓ | ✓ | |
| UC10 | Xem chi tiết sản phẩm | ✓ | ✓ | |
| UC11 | Cập nhật sản phẩm | ✓ | ✗ | |
| UC12 | Ngừng kinh doanh sản phẩm | ✓ | ✗ | |
| UC13 | Tìm kiếm sản phẩm | ✓ | ✓ | |
| UC14 | Lọc và sắp xếp sản phẩm | ✓ | ✓ | |
| UC15 | Kiểm tra trùng sản phẩm | Tự động | Tự động | Kiểm tra SKU, barcode khi Admin lưu |
| UC16 | Tạo mã vạch/QR | ✓ | ✓ | |
| UC17 | In mã vạch/QR | ✓ | ✓ | |
| UC18 | Thêm danh mục | ✓ | ✗ | |
| UC19 | Xem danh sách danh mục | ✓ | ✓ | |
| UC20 | Cập nhật danh mục | ✓ | ✗ | |
| UC21 | Ngừng sử dụng danh mục | ✓ | ✗ | |
| UC22 | Tìm kiếm danh mục | ✓ | ✓ | |
| UC23 | Thêm nhà cung cấp | ✓ | ✗ | |
| UC24 | Xem danh sách nhà cung cấp | ✓ | ✓ | |
| UC25 | Xem chi tiết nhà cung cấp | ✓ | ✓ | |
| UC26 | Cập nhật nhà cung cấp | ✓ | ✗ | |
| UC27 | Ngừng hợp tác nhà cung cấp | ✓ | ✗ | |
| UC28 | Tìm kiếm nhà cung cấp | ✓ | ✓ | |

---

### III. Quản lý tồn kho

| UC | Chức năng | Admin | Nhân viên | Ghi chú |
|----|-----------|-------|-----------|---------|
| UC29 | Xem danh sách tồn kho | ✓ | ✓ | |
| UC30 | Xem chi tiết tồn kho | ✓ | ✓ | |
| UC31 | Theo dõi hàng giữ chỗ | ✓ | Giới hạn | Nhân viên chỉ xem, Admin được cập nhật reserved_quantity |
| UC32 | Tính số lượng khả dụng | Tự động | Tự động | Cột sinh tự động trong inventory |
| UC33 | Thiết lập ngưỡng tồn kho | ✓ | ✗ | Đặt min_stock, max_stock |
| UC34 | Cảnh báo tồn kho thấp | ✓ | ✓ | |
| UC35 | Cảnh báo tồn kho vượt mức | ✓ | ✓ | |
| UC36 | Xem sổ kho | ✓ | ✓ | |
| UC37 | Tra cứu lịch sử biến động | ✓ | ✓ | |
| UC38 | Truy vết giao dịch tồn kho | ✓ | ✓ | |
| UC39 | Tính giá trị tồn kho | ✓ | ✗ | Dựa trên giá vốn, chỉ Admin |

---

### IV. Quản lý nhập, xuất và kiểm kê

| UC | Chức năng | Admin | Nhân viên | Ghi chú |
|----|-----------|-------|-----------|---------|
| UC40 | Tạo phiếu nhập kho | ✓ | ✓ | Phiếu mới ở trạng thái DRAFT |
| UC41 | Thêm sản phẩm vào phiếu nhập | ✓ | ✓ | |
| UC42 | Cập nhật phiếu nhập | ✓ | Giới hạn | Nhân viên chỉ sửa phiếu do mình lập, khi còn DRAFT |
| UC43 | Xem chi tiết phiếu nhập | ✓ | ✓ | |
| UC44 | Gửi duyệt phiếu nhập | ✓ | Giới hạn | Nhân viên chỉ gửi phiếu do mình lập |
| UC45 | Phê duyệt phiếu nhập | ✓ | ✗ | Admin không tự duyệt phiếu do chính mình lập |
| UC46 | Xác nhận nhập kho | ✓ | ✓ | Chỉ khi phiếu đã được duyệt; chống ghi nhận lặp |
| UC47 | Tính giá vốn bình quân | ✓ | ✗ | Chạy tự động khi xác nhận nhập; chỉ Admin xem kết quả |
| UC48 | Xem lịch sử nhập kho | ✓ | ✓ | |
| UC49 | Tạo phiếu xuất kho | ✓ | ✓ | Phiếu mới ở trạng thái DRAFT |
| UC50 | Chọn loại xuất kho | ✓ | ✓ | Bán, hủy, nội bộ; mọi loại đều phải qua duyệt |
| UC51 | Thêm sản phẩm vào phiếu xuất | ✓ | ✓ | |
| UC52 | Kiểm tra tồn kho khi xuất | Tự động | Tự động | Kiểm tra available_quantity theo kho và lô |
| UC53 | Cập nhật phiếu xuất | ✓ | Giới hạn | Nhân viên chỉ sửa phiếu do mình lập, khi còn DRAFT |
| UC54 | Gửi duyệt phiếu xuất | ✓ | Giới hạn | Nhân viên chỉ gửi phiếu do mình lập |
| UC55 | Phê duyệt phiếu xuất | ✓ | ✗ | Admin không tự duyệt phiếu do chính mình lập |
| UC56 | Xác nhận xuất kho | ✓ | ✓ | Chỉ khi phiếu đã được duyệt; kiểm tra tồn trong cùng transaction SQL |
| UC57 | Hủy phiếu xuất | ✓ | Giới hạn | Nhân viên chỉ hủy phiếu do mình lập, chưa hoàn tất và chưa đổi tồn kho |
| UC58 | Xem lịch sử xuất kho | ✓ | ✓ | |
| UC59 | Tạo phiếu kiểm kê | ✓ | ✓ | |
| UC60 | Lập danh sách kiểm kê | ✓ | ✓ | |
| UC61 | Nhập số lượng thực tế | ✓ | ✓ | |
| UC62 | Tính chênh lệch tồn kho | Tự động | Tự động | difference_quantity = actual_quantity - system_quantity |
| UC63 | Xem kết quả kiểm kê | ✓ | ✓ | |
| UC64 | Xác nhận kết quả kiểm kê | ✓ | ✗ | Duyệt hoặc từ chối, lưu người duyệt |
| UC65 | Tạo phiếu điều chỉnh tồn kho | ✓ | ✗ | Điều chỉnh tồn không qua phiếu nhập xuất |
| UC66 | Cập nhật tồn kho sau kiểm kê | ✓ | ✗ | Chỉ áp dụng chênh lệch đã được duyệt |
| UC67 | Xem lịch sử kiểm kê | ✓ | ✓ | |

---

### V. Chức năng nâng cao (quét mã, lô hàng, trợ lý AI)

| UC | Chức năng | Admin | Nhân viên | Ghi chú |
|----|-----------|-------|-----------|---------|
| UC68 | Quét mã sản phẩm | ✓ | ✓ | |
| UC69 | Tra cứu nhanh bằng mã | ✓ | ✓ | |
| UC70 | Thêm sản phẩm vào phiếu bằng mã | ✓ | ✓ | Theo quyền sửa phiếu ở UC42, UC53 |
| UC71 | Kiểm kê bằng mã | ✓ | ✓ | |
| UC72 | Xử lý mã không hợp lệ | Tự động | Tự động | |
| UC73 | Quản lý lô hàng | ✓ | ✓ | Nhân viên ghi nhận lô khi lập phiếu nhập |
| UC74 | Ghi nhận ngày sản xuất | ✓ | ✓ | |
| UC75 | Ghi nhận hạn sử dụng | ✓ | ✓ | |
| UC76 | Theo dõi số lượng theo lô | Tự động | Tự động | Cập nhật khi xác nhận nhập, xuất |
| UC77 | Cảnh báo hàng cận hạn | ✓ | ✓ | |
| UC78 | Cảnh báo hàng hết hạn | ✓ | ✓ | |
| UC79 | Tra cứu lô hàng | ✓ | ✓ | |
| UC80 | Hỗ trợ chọn lô xuất kho | ✓ | ✓ | Đề xuất lô theo FEFO |
| UC81 | Mở giao diện chatbot | ✓ | ✓ | |
| UC82 | Nhập câu hỏi | ✓ | ✓ | |
| UC83 | Tra cứu tài liệu nội bộ | ✓ | ✓ | |
| UC84 | Trả lời dựa trên tài liệu | ✓ | ✓ | Chatbot không tự thực hiện giao dịch kho |
| UC85 | Hiển thị nguồn tham khảo | ✓ | ✓ | |
| UC86 | Lưu lịch sử hội thoại | ✓ | Giới hạn | Mỗi người chỉ xem lịch sử của chính mình |
| UC87 | Kiểm soát quyền truy cập tài liệu | Tự động | Tự động | Schema chưa hỗ trợ phân quyền riêng từng tài liệu |
| UC88 | Xử lý câu hỏi không có dữ liệu | Tự động | Tự động | |

---

### VI. Báo cáo và thống kê

| UC | Chức năng | Admin | Nhân viên | Ghi chú |
|----|-----------|-------|-----------|---------|
| UC89 | Xem Dashboard | ✓ | Giới hạn | Nhân viên không thấy thẻ giá trị tồn kho |
| UC90 | Thống kê số lượng sản phẩm | ✓ | ✓ | |
| UC91 | Thống kê giá trị tồn kho | ✓ | ✗ | Dựa trên giá vốn, chỉ Admin |
| UC92 | Thống kê hàng sắp hết | ✓ | ✓ | |
| UC93 | Thống kê hàng vượt mức tồn | ✓ | ✓ | |
| UC94 | Thống kê hàng cận hạn | ✓ | ✓ | |
| UC95 | Thống kê nhập – xuất | ✓ | ✓ | |
| UC96 | Thống kê sản phẩm biến động nhiều | ✓ | ✓ | |
| UC97 | Lọc dữ liệu Dashboard | ✓ | ✓ | |
| UC98 | Báo cáo tồn kho | ✓ | ✓ | |
| UC99 | Báo cáo nhập kho | ✓ | ✓ | |
| UC100 | Báo cáo xuất kho | ✓ | ✓ | |
| UC101 | Báo cáo biến động kho | ✓ | ✓ | |
| UC102 | Báo cáo hàng cận hạn | ✓ | ✓ | |
| UC103 | Báo cáo chênh lệch kiểm kê | ✓ | ✓ | |
| UC104 | Lọc báo cáo theo thời gian | ✓ | ✓ | |
| UC105 | Lọc báo cáo theo điều kiện | ✓ | ✓ | |
| UC106 | Xuất báo cáo Excel | ✓ | Giới hạn | Nhân viên chỉ xuất báo cáo mình được xem |
| UC107 | Xuất báo cáo PDF | ✓ | Giới hạn | Nhân viên chỉ xuất báo cáo mình được xem |
| UC108 | In phiếu nhập kho | ✓ | ✓ | |
| UC109 | In phiếu xuất kho | ✓ | ✓ | |
| UC110 | In phiếu kiểm kê | ✓ | ✓ | |
| UC111 | Xuất dữ liệu theo bộ lọc | ✓ | Giới hạn | Nhân viên chỉ xuất dữ liệu mình được xem; không gồm giá vốn và giá trị tồn |

---

## 6. Quy tắc theo vòng đời phiếu

Phiếu nhập, xuất và kiểm kê dùng chung các trạng thái trong bảng `transactions`: `DRAFT`, `PENDING` (đã gửi duyệt), đã duyệt hoặc bị từ chối, rồi hoàn tất. Quyền theo từng bước:

| Bước | Trạng thái | Admin | Nhân viên |
|------|-----------|-------|-----------|
| Lập phiếu, thêm dòng hàng | `DRAFT` | ✓ | ✓ |
| Sửa phiếu | `DRAFT` | ✓ | Chỉ phiếu do mình lập |
| Gửi duyệt | `DRAFT` → `PENDING` | ✓ | Chỉ phiếu do mình lập |
| Duyệt hoặc từ chối (ghi `approved_by`, `rejection_reason`) | `PENDING` | ✓ | ✗ |
| Xác nhận nhập hoặc xuất kho (cập nhật `inventory`) | Đã duyệt → hoàn tất | ✓ | ✓ |
| Hủy phiếu | Chưa hoàn tất | ✓ | Chỉ phiếu do mình lập, chưa đổi tồn |
| Sửa phiếu đã hoàn tất | Hoàn tất | ✗ | ✗ |

**Hai quy tắc áp dụng cho cả hai vai trò:**
- Người duyệt phải khác người lập phiếu
- Phiếu đã hoàn tất không được sửa, chỉ được sửa sai bằng phiếu điều chỉnh (UC65) do Admin lập

**Với kiểm kê:**  
Nhân viên đếm và nhập số lượng thực tế (UC59–UC63), Admin xác nhận kết quả (UC64) rồi mới lập điều chỉnh và cập nhật tồn (UC65, UC66).

---

## 7. Áp dụng vào ứng dụng và backend

### Menu trên ứng dụng Android

Theo thiết kế thanh điều hướng dưới và menu 3 gạch:

| Mục | Admin | Nhân viên |
|-----|-------|-----------|
| Tổng quan | ✓ | ✓ (ẩn thẻ giá trị tồn kho) |
| Kho hàng | ✓ | ✓ |
| Phiếu | ✓ (có nút duyệt) | ✓ (không có nút duyệt) |
| Báo cáo | ✓ | ✓ (không có báo cáo giá trị tồn) |
| Tài khoản | ✓ | ✓ |
| **Menu:** Sản phẩm, Danh mục, Nhà cung cấp | ✓ thêm, sửa, ngừng | Chỉ xem, tìm, lọc |
| **Menu:** Lô hàng, Cảnh báo, Quét mã, Trợ lý AI | ✓ | ✓ |
| **Menu:** Kiểm kê | ✓ (có nút xác nhận kết quả) | ✓ (không có nút xác nhận) |
| **Menu:** Người dùng | ✓ | Ẩn hoàn toàn |

---

### Gợi ý cho backend Ktor

#### 1. Đưa vai trò vào JWT claim
Đưa vai trò vào claim của access token và đọc lại ở mỗi request, hoặc tra từ bảng `users` theo user_id trong token.

#### 2. Hàm kiểm tra vai trò dùng chung
Viết một hàm kiểm tra vai trò dùng chung (ví dụ `requireRole(ADMIN)`) và gắn vào từng route của ma trận ở mục 5, thay vì kiểm tra rải rác trong từng handler.

**Ví dụ:**
```kotlin
fun Route.requireRole(role: String, build: Route.() -> Unit): Route {
    return intercept(ApplicationCallPipeline.Features) {
        val principal = call.principal<UserPrincipal>()
        if (principal?.role != role) {
            call.respond(HttpStatusCode.Forbidden, "Không có quyền truy cập")
            finish()
        }
    }.apply(build)
}
```

#### 3. Kiểm tra ownership và trạng thái
Với các dòng **Giới hạn**, kiểm tra thêm điều kiện sở hữu (cột người lập của phiếu so với user_id trong token) và trạng thái phiếu ở tầng service.

#### 4. HTTP Status Code
- Trả về **403 Forbidden** khi sai quyền
- Trả về **401 Unauthorized** khi token không hợp lệ, hết hạn hoặc đã thu hồi

#### 5. Audit log
Ghi lại các thao tác nhạy cảm (duyệt, từ chối, điều chỉnh tồn, đổi vai trò) cùng `user_id` và thời điểm.

---

## 8. Các điểm cần xác nhận

Đây là những chỗ đặc tả gốc để ngỏ, tài liệu đã chọn một phương án để bạn xem lại:

### ❓ 1. Nhân viên có được sửa danh mục sản phẩm hay không?
**Tài liệu đề xuất**: Không, chỉ Admin (UC08, UC11).

### ❓ 2. Nhân viên xác nhận nhập, xuất kho sau khi Admin duyệt
**Tài liệu đề xuất**: Nhân viên được xác nhận (UC46, UC56).  
Nếu muốn Admin làm luôn bước này, đổi UC46 và UC56 của nhân viên thành **✗**.

### ❓ 3. Giới hạn nhân viên theo kho
Schema không có bảng gán kho cho từng nhân viên, nên hiện **mọi nhân viên thấy tất cả kho**. Muốn giới hạn theo kho cần thêm bảng gán kho, mà đặc tả đang giữ nguyên 11 bảng.

### ❓ 4. Nhân viên có được xem giá vốn và giá trị tồn kho hay không?
**Tài liệu đề xuất**: Không (UC39, UC47, UC91).

### ❓ 5. Admin duyệt phiếu của chính mình
Nếu hệ thống **chỉ có một Admin** thì quy tắc "người duyệt khác người lập" sẽ chặn Admin duyệt phiếu của chính mình.  
**Giải pháp**:
- Thêm Admin thứ hai, hoặc
- Nới quy tắc này cho Admin

### ❓ 6. Có cần vai trò MANAGER riêng không?
Có cần vai trò `MANAGER` riêng để tách người duyệt phiếu khỏi người quản lý tài khoản hay không?

---

**Tài liệu được tạo từ**: Đặc tả 111 UC – warehouse_db  
**Áp dụng cho**: Backend Kotlin/Ktor + Android Jetpack Compose  
**Database**: PostgreSQL với 11 bảng  
**Ngày tạo**: 2026-11-03
