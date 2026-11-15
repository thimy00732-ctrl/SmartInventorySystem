package com.sis.android.ui.inventory

import com.sis.android.data.model.ExpiryStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

/*
 * Bộ định dạng dùng cho màn hình quản lý tồn kho.
 * Chức năng chính là chuẩn hóa dữ liệu để hiển thị đúng định dạng số, ngày tháng,
 * trạng thái hết hạn và nhãn lô theo chuẩn nghiệp vụ của dự án.
 */
object InventoryFormatters {
    private val numberFormat = DecimalFormat("#,###")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun formatNumber(value: Number): String = numberFormat.format(value)

    fun formatDate(value: String?): String = value?.let {
        runCatching { LocalDate.parse(it).format(dateFormatter) }.getOrDefault("-")
    } ?: "-"

    fun formatDateTime(value: String?): String = value?.let {
        runCatching { LocalDateTime.parse(it).format(dateTimeFormatter) }.getOrDefault("-")
    } ?: "-"

    fun formatBatch(value: String?): String = value?.ifBlank { null } ?: "Không theo lô"

    fun formatExpiryStatus(status: ExpiryStatus): String = when (status) {
        ExpiryStatus.NONE -> "Không có hạn"
        ExpiryStatus.EXPIRED -> "Hết hạn"
        ExpiryStatus.NEAR_EXPIRY -> "Cận hạn"
        ExpiryStatus.OK -> "Bình thường"
    }

    fun formatCurrency(value: BigDecimal): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        return formatter.format(value.setScale(0, RoundingMode.HALF_UP))
    }
}
