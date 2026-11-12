package com.sis.backend.service

import com.sis.backend.dto.AlertSummaryResponse
import com.sis.backend.dto.LowStockAlertResponse
import com.sis.backend.dto.OverStockAlertResponse
import com.sis.backend.repository.AlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service xử lý nghiệp vụ cảnh báo tồn kho (UC34, UC35).
 * 
 * @property alertRepository Repository cảnh báo.
 */
class AlertService(
    private val alertRepository: AlertRepository
) {
    
    /**
     * Lấy danh sách sản phẩm có tồn kho thấp (UC34).
     * 
     * Điều kiện: totalStock < min_stock.
     * Chỉ tính kho đang hoạt động.
     * 
     * @return Danh sách sản phẩm tồn thấp, sắp xếp theo missingQuantity giảm dần.
     */
    suspend fun getLowStockAlerts(): List<LowStockAlertResponse> {
        return withContext(Dispatchers.IO) {
            alertRepository.findLowStockAlerts()
        }
    }
    
    /**
     * Lấy danh sách sản phẩm có tồn kho vượt mức (UC35).
     * 
     * Điều kiện: totalStock > max_stock.
     * Chỉ tính kho đang hoạt động.
     * 
     * @return Danh sách sản phẩm tồn vượt mức, sắp xếp theo excessPercent giảm dần.
     */
    suspend fun getOverStockAlerts(): List<OverStockAlertResponse> {
        return withContext(Dispatchers.IO) {
            alertRepository.findOverStockAlerts()
        }
    }
    
    /**
     * Lấy tổng quan cảnh báo (UC35).
     * 
     * Trả về số lượng sản phẩm tồn thấp, vượt mức, và tổng tồn kho.
     * 
     * @return Thống kê tổng quan.
     */
    suspend fun getAlertSummary(): AlertSummaryResponse {
        return withContext(Dispatchers.IO) {
            alertRepository.getAlertSummary()
        }
    }
}
