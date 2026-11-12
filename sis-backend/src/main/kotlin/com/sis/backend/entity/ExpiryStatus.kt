package com.sis.backend.entity

/**
 * Trạng thái hết hạn của lô hàng trong tồn kho.
 * 
 * Dùng để phân loại và cảnh báo sản phẩm gần hết hạn hoặc đã hết hạn.
 * Tính toán dựa trên expiry_date so với ngày hiện tại và ngưỡng cận hạn.
 */
enum class ExpiryStatus {
    /**
     * Không có ngày hết hạn (expiry_date = null).
     * Ví dụ: sản phẩm không hư hỏng, hàng điện tử.
     */
    NONE,
    
    /**
     * Đã hết hạn (expiry_date < hôm nay).
     * Cần kiểm tra và xử lý ngay (thanh lý, huỷ bỏ).
     */
    EXPIRED,
    
    /**
     * Gần hết hạn (còn <= nearExpiryDays ngày).
     * Cần ưu tiên xuất kho hoặc khuyến mại.
     * Ngưỡng mặc định: 30 ngày (cấu hình trong application.conf).
     */
    NEAR_EXPIRY,
    
    /**
     * Còn hạn tốt (còn > nearExpiryDays ngày).
     * Tình trạng bình thường.
     */
    OK
}
