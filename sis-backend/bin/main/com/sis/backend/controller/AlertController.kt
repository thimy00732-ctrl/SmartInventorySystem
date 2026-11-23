package com.sis.backend.controller

import com.sis.backend.dto.AlertSummaryResponse
import com.sis.backend.dto.LowStockAlertResponse
import com.sis.backend.dto.OverStockAlertResponse
import com.sis.backend.service.AlertService

/**
 * Controller xử lý các request liên quan đến cảnh báo tồn kho (UC34, UC35).
 * 
 * @property alertService Service xử lý nghiệp vụ cảnh báo.
 */
class AlertController(
    private val alertService: AlertService
) {
    
    /**
     * Lấy danh sách sản phẩm có tồn kho thấp (UC34).
     * 
     * GET /api/alerts/low-stock
     * 
     * @return Danh sách sản phẩm tồn thấp.
     */
    suspend fun getLowStockAlerts(): List<LowStockAlertResponse> {
        return alertService.getLowStockAlerts()
    }
    
    /**
     * Lấy danh sách sản phẩm có tồn kho vượt mức (UC35).
     * 
     * GET /api/alerts/over-stock
     * 
     * @return Danh sách sản phẩm tồn vượt mức.
     */
    suspend fun getOverStockAlerts(): List<OverStockAlertResponse> {
        return alertService.getOverStockAlerts()
    }
    
    /**
     * Lấy tổng quan cảnh báo (UC35).
     * 
     * GET /api/alerts/summary
     * 
     * @return Thống kê tổng quan.
     */
    suspend fun getAlertSummary(): AlertSummaryResponse {
        return alertService.getAlertSummary()
    }
}
