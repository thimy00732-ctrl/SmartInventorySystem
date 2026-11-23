# YÊU CẦU XÂY DỰNG MODULE GIAO DIỆN: QUẢN LÝ TỒN KHO (NHÓM III: UC29–UC35, UC39)

## 1. QUY TẮC BẮT BUỘC KHI TRIỂN KHAI (WORKFLOW & GOVERNANCE RULES)
1. **Tuyệt đối KHÔNG tự ý làm hết hoặc tự suy diễn:** 
   - Không được tự động viết toàn bộ code tất cả màn hình trong một lần duy nhất.
   - Trước khi bắt đầu Đợt 1, nếu cần làm rõ về tech stack, thư viện UI hoặc cấu trúc thư mục hiện tại của dự án, **bắt buộc phải hỏi lại người dùng** và chờ xác nhận.
   - Trong quá trình làm, nếu gặp bất kỳ điểm nào chưa rõ về nghiệp vụ hoặc giao diện, phải dừng lại hỏi thay vì tự ý quyết định.
2. **Tuân thủ tuyệt đối giao diện & quy ước hiện có:**
   - Phải kế thừa và giữ nguyên layout, màu sắc, component style của hệ thống giao diện cũ đang có trong dự án.
   - Giữ nguyên vẹn các quy chuẩn định dạng, tên cột, tên nhãn, logic tính toán đã đặc tả bên dưới. Không tự ý đổi tên trường dữ liệu hay bỏ sót cột hiển thị.
3. **Chia nhỏ dự án thành 4 đợt (Phased Implementation):**
   - **Đợt 1:** Khảo sát cấu trúc dự án cũ -> Tạo khung cấu trúc dữ liệu (`types`, `formatters`, `mock data`) và **Màn hình 1 (Danh sách tồn kho - UC29)** kèm filter/sort/pagination. *(Dừng lại báo cáo)*
   - **Đợt 2:** Xây dựng **Màn hình 2 (Chi tiết tồn kho & Cập nhật giữ chỗ - UC30, UC31, UC32)** dạng Drawer và bảng các kho/lô khác. *(Dừng lại báo cáo)*
   - **Đợt 3:** Xây dựng **Màn hình 3 (Thiết lập ngưỡng tồn kho - UC33)** và **Màn hình 4 & 5 (Trang Cảnh báo tồn kho 2 tab: Tồn thấp UC34 & Vượt mức UC35 + Drawer drill-down)**. *(Dừng lại báo cáo)*
   - **Đợt 4:** Xây dựng **Màn hình 6 (Giá trị tồn kho - UC39)**, tích hợp phân quyền hiển thị (`ADMIN` vs `WAREHOUSE_STAFF`), kiểm tra responsive và nghiệm thu tổng thể. *(Dừng lại báo cáo hoàn thành)*
4. **Chế độ báo cáo bắt buộc sau mỗi đợt:**
   - Sau khi hoàn thành code của từng đợt, Agent **phải dừng lại ngay** và gửi báo cáo gồm:
     - Danh sách các file đã tạo mới hoặc chỉnh sửa.
     - Các chức năng đã hoàn thành trong đợt.
     - Checklist xác nhận tuân thủ giao diện cũ & quy ước hiển thị (`dd/MM/yyyy`, phân tách hàng nghìn, tiền VND, nhãn `"Không theo lô"`).
     - Các câu hỏi cần xác nhận (nếu có) và xin phép triển khai đợt tiếp theo.

---

## 2. QUY ƯỚC CHUNG & THÀNH PHẦN DÙNG CHUNG (GLOBAL RULES)
1. **Định dạng hiển thị:**
   - **Mã lô (`batch_code`):** Nếu giá trị `null` hoặc chuỗi rỗng `""`, hiển thị nhãn badge màu xám nhạt: `Không theo lô`.
   - **Ngày tháng (`expiry_date`, `mfg_date`, `updated_at`):** Định dạng `dd/MM/yyyy` (hoặc `dd/MM/yyyy HH:mm` cho `updated_at`).
   - **Số lượng:** Định dạng số nguyên có dấu phân tách hàng nghìn (VD: `1,250` hoặc `1.250`).
   - **Tiền tệ:** Định dạng theo VND (VD: `15.500.000 ₫` hoặc `15,500,000 VND`).
2. **Menu điều hướng (Sidebar/Navbar):**
   - Nhóm menu cha: **Tồn kho**
   - Các menu con:
     1. Danh sách tồn kho
     2. Cảnh báo tồn kho (kèm tổng badge số lượng cảnh báo)
     3. Giá trị tồn kho
     4. Thiết lập ngưỡng tồn kho *(nếu tách màn riêng; đồng thời tích hợp sẵn vào trang Chi tiết/Sửa sản phẩm)*
3. **Phân quyền hiển thị (Role-based UI):**
   - `ADMIN`: Thấy toàn bộ menu, nút **"Cập nhật giữ chỗ"**, màn hình/form sửa ngưỡng `min_stock` / `max_stock`, và màn hình **Giá trị tồn kho** (chứa `cost_price` nhạy cảm).
   - `WAREHOUSE_STAFF`: Chỉ xem được **Danh sách tồn kho**, **Chi tiết tồn kho** (ẩn nút cập nhật giữ chỗ), và **Cảnh báo tồn kho**. Ẩn hoàn toàn menu/trang **Giá trị tồn kho** và quyền sửa ngưỡng.
4. **Trạng thái giao diện & Responsive:**
   - Mọi bảng dữ liệu và trang đều phải hỗ trợ 4 trạng thái: **Loading** (Skeleton/Spinner), **Empty** (thông báo rõ ràng), **Error** (thông báo lỗi + nút `"Thử lại"`), và **Success**.
   - Responsive: Tất cả bảng dữ liệu phải hỗ trợ cuộn ngang (`overflow-x-auto`) trên màn hình nhỏ.

---

## 3. CHI TIẾT TỪNG MÀN HÌNH

### MÀN HÌNH 1: DANH SÁCH TỒN KHO (UC29)
- **Thanh lọc phía trên (Filter Bar):**
  - Ô tìm kiếm: tìm theo `SKU`, `barcode`, hoặc `tên sản phẩm`.
  - Dropdown **Kho**: mặc định chọn `"Tất cả kho"`.
  - Dropdown **Danh mục**: mặc định `"Tất cả danh mục"`.
  - Dropdown **Trạng thái tồn**: `Tất cả` / `Còn hàng` (`available_quantity > 0`) / `Hết hàng` (`stock_quantity === 0`) / `Có giữ chỗ` (`reserved_quantity > 0`).
  - Nút **"Xóa bộ lọc"**: reset toàn bộ filter về mặc định.
- **Bảng dữ liệu (Data Table):**
  | Cột | Nguồn dữ liệu | Quy tắc hiển thị |
  | :--- | :--- | :--- |
  | Hình ảnh | `products.image_url` | Ảnh nhỏ (thumbnail), có ảnh mặc định khi trống |
  | SKU | `products.sku` | Mã SKU sản phẩm |
  | Tên sản phẩm | `products.product_name` | Cho phép sắp xếp (Sort) |
  | Kho | `warehouses.warehouse_name` | Tên kho |
  | Mã lô | `inventory.batch_code` | Hiển thị `"Không theo lô"` nếu rỗng |
  | Hạn sử dụng | `inventory.expiry_date` | Định dạng `dd/MM/yyyy`. Tô màu khi cận hạn/hết hạn (hiển thị trước, logic cảnh báo ở UC77–78) |
  | Tồn thực tế | `stock_quantity` | Phân tách hàng nghìn, cho phép sắp xếp (Sort) |
  | Giữ chỗ | `reserved_quantity` | Phân tách hàng nghìn |
  | Khả dụng | `available_quantity` | **In đậm**, phân tách hàng nghìn (`= stock_quantity - reserved_quantity`) |
  | Đơn vị | `products.unit` | Đơn vị tính |
  | Thao tác | - | Nút **"Chi tiết"** (mở Màn hình 2) |
- **Tính năng đi kèm:** Phân trang (Pagination), sắp xếp theo **Tên sản phẩm**, **Tồn thực tế**, **Hạn sử dụng**, hiển thị tổng số dòng.
- **Trạng thái rỗng:** Hiển thị thông báo chính xác: `"Chưa có dữ liệu tồn kho"`.

---

### MÀN HÌNH 2: CHI TIẾT TỒN KHO & GIỮ CHỖ (UC30, UC31, UC32)
- **Hình thức hiển thị:** Dạng trang hoặc Drawer mở từ danh sách.
- **Khối 1: Thông tin sản phẩm:**
  - Hình ảnh, `SKU`, `barcode`, `tên`, `danh mục`, `nhà cung cấp`, `đơn vị`, `trạng thái`, `ngưỡng min/max hiện tại`.
- **Khối 2: Thông tin dòng tồn đang xem:**
  - Kho, mã lô (hoặc `"Không theo lô"`), ngày sản xuất (`dd/MM/yyyy`), hạn sử dụng (`dd/MM/yyyy`).
  - **3 ô số lớn:** `Tồn thực tế`, `Giữ chỗ`, `Khả dụng` (ô Khả dụng ghi chú công thức `"= Tồn thực tế − Giữ chỗ"`).
  - Cập nhật lần cuối (`updated_at`).
  - **Lưu ý nghiệp vụ:** Giao diện tuyệt đối **KHÔNG** có ô sửa `stock_quantity` (theo quy tắc tồn chỉ đổi qua phiếu).
- **Chức năng cập nhật giữ chỗ (UC31):**
  - Nút **"Cập nhật giữ chỗ"** chỉ hiện với người có quyền.
  - Form: ô nhập `reserved_quantity` mới, hiển thị bên cạnh giá trị tối đa cho phép (`= tồn thực tế`), ô ghi chú lý do (nếu BE có lưu).
  - Validation hiển thị ngay dưới ô nhập: không âm (`>= 0`), không vượt tồn thực tế (`<= stock_quantity`).
  - Sau khi lưu: `khả dụng` tự động cập nhật.
- **Khối 3: "Các kho/lô khác của sản phẩm này":**
  - Bảng nhỏ gồm: `Kho`, `Mã lô`, `Hạn dùng`, `Tồn thực tế`, `Giữ chỗ`, `Khả dụng`, kèm **dòng tổng cộng** ở cuối bảng.

---

### MÀN HÌNH 3: THIẾT LẬP NGƯỠNG TỒN KHO (UC33)
- Hỗ trợ **Cách 1 (khuyến nghị):** gộp vào trang chi tiết/sửa sản phẩm; hoặc **Cách 2:** màn hình riêng dạng bảng sửa nhanh.
- **Cấu trúc bảng:**
  | Cột | Ghi chú |
  | :--- | :--- |
  | SKU, Tên sản phẩm | Chỉ đọc |
  | Tổng tồn hiện tại | `SUM` mọi kho/lô, chỉ đọc |
  | Tồn tối thiểu (`min_stock`) | Ô nhập số nguyên `>= 0` |
  | Tồn tối đa (`max_stock`) | Ô nhập số nguyên `>= min_stock` |
  | Thao tác | Nút **Lưu** / **Hủy** |
- **Validation:** Số nguyên không âm, `max_stock >= min_stock` (hiển thị lỗi ngay dưới ô). Khi lưu thành công hiện thông báo và cập nhật lại trạng thái cảnh báo.

---

### MÀN HÌNH 4 & 5 (GỘP): CẢNH BÁO TỒN KHO (UC34 & UC35)
- **Bố cục:** Gộp thành một trang **"Cảnh báo tồn kho"** với **hai tab** (`Tồn thấp` / `Vượt mức`), mỗi tab có badge số lượng.
- **Tab 1: Cảnh báo tồn kho thấp (UC34):**
  - **Đầu trang:** Thẻ tổng quan `"X sản phẩm dưới ngưỡng"`, trong đó `"Y sản phẩm đã hết hàng (tồn = 0)"`. Chú thích ngắn: `"Tính theo tổng tồn của các kho đang hoạt động"`.
  - **Bộ lọc:** Danh mục, nhà cung cấp, ô tìm kiếm.
  - **Bảng dữ liệu (sắp xếp mặc định theo `Số lượng thiếu` giảm dần):**
    - `SKU`, `Tên sản phẩm`, `Đơn vị` (`products`)
    - `Nhà cung cấp` (`suppliers` - hỗ trợ đặt hàng bổ sung)
    - `Tồn tối thiểu` (`min_stock`)
    - `Tổng tồn hiện tại` (`SUM(stock_quantity)`)
    - `Số lượng thiếu` (`missing_qty`)
    - `Mức độ`: Badge **Hết hàng** (đỏ, `tồn = 0`) / **Thấp** (cam)
    - `Thao tác`: Nút **"Xem phân bổ theo kho"**
  - **Drawer Drill-down:** Bấm `"Xem phân bổ theo kho"` mở drawer hiển thị: `Kho`, `Mã lô`, `Hạn dùng`, `Tồn thực tế`, `Khả dụng`.
- **Tab 2: Cảnh báo tồn kho vượt mức (UC35):**
  - **Bộ lọc:** Danh mục, nhà cung cấp, ô tìm kiếm.
  - **Bảng dữ liệu:**
    - `SKU`, `Tên sản phẩm`, `Đơn vị` (`products`)
    - `Tồn tối đa` (`max_stock`)
    - `Tổng tồn hiện tại` (`SUM(stock_quantity)`)
    - `Số lượng vượt` (`excess_qty`)
    - `Tỷ lệ vượt`: `(tổng tồn / max_stock − 1) × 100%` (tùy chọn)
    - `Thao tác`: Nút **"Xem kho bị ảnh hưởng"**
  - **Drawer Drill-down ("Kho bị ảnh hưởng"):** Hiển thị `Kho`, `Mã lô`, `Hạn dùng`, `Tồn thực tế`, `Khả dụng`, sắp xếp theo `Tồn` giảm dần.

---

### MÀN HÌNH 6: GIÁ TRỊ TỒN KHO (UC39)
- *(Chỉ hiển thị với user có quyền xem giá vốn)*
- **Ghi chú bắt buộc hiển thị rõ:** `"Giá trị tính theo giá vốn hiện tại, không phản ánh giá vốn lịch sử."`
- **Thẻ tổng quan:** Tổng giá trị tồn toàn hệ thống, tổng số lượng, số sản phẩm có tồn, giá trị theo từng kho (thẻ nhỏ hoặc biểu đồ cột).
- **Bộ lọc:** Kho, danh mục, ô tìm kiếm.
- **Bảng chi tiết:**
  - `Kho` (`warehouse_name`)
  - `SKU`, `Tên sản phẩm` (`products`)
  - `Tổng số lượng` (`SUM(stock_quantity)`)
  - `Giá vốn hiện tại` (`cost_price` - hiển thị VND)
  - `Giá trị tồn` (`số lượng × giá vốn` - hiển thị VND)
  - **Dòng tổng cộng:** Cuối bảng hoặc theo từng kho.