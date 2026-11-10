package com.sis.android.data.repository

import com.sis.android.data.model.InventoryItemResponse
import com.sis.android.data.model.ReservationResponse
import com.sis.android.data.model.UpdateReservationRequest
import com.sis.android.data.remote.ApiService
import org.json.JSONObject

class InventoryRepository(
    private val apiService: ApiService
) {

    suspend fun getInventoryList(): Result<List<InventoryItemResponse>> {
        return try {
            val response = apiService.getInventoryList()
            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                Result.success(body)
            } else {
                Result.failure(Exception("Lỗi máy chủ (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Không thể kết nối máy chủ"))
        }
    }

    suspend fun updateReservation(
        inventoryId: Int,
        reservedQuantity: Int
    ): Result<ReservationResponse> {

        return try {
            val response = apiService.updateReservation(
                inventoryId,
                UpdateReservationRequest(reservedQuantity)
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(
                        Exception("Backend không trả về dữ liệu")
                    )
                }
            } else {
                val errorText = response.errorBody()?.string()
                val message = try {
                    JSONObject(errorText ?: "{}")
                        .optString("message", "Cập nhật thất bại")
                } catch (_: Exception) {
                    "Cập nhật thất bại (${response.code()})"
                }

                Result.failure(Exception(message))
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: "Không thể kết nối máy chủ")
            )
        }
    }
}
