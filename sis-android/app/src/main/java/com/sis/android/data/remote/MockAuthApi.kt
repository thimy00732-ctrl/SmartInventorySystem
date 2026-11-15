
package com.sis.android.data.remote

import kotlinx.coroutines.delay
import java.io.IOException

class MockAuthApi {

    suspend fun login(email: String, password: String): String {
        // Mô phỏng thời gian phản hồi của máy chủ
        delay(1500)

        // Mô phỏng lỗi mạng
        if (email.trim().equals("offline", ignoreCase = true)) {
            throw IOException(
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng."
            )
        }

        // Kiểm tra email và mật khẩu trống
        if (email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException(
                "Vui lòng nhập đầy đủ email và mật khẩu."
            )
        }

        val normalizedEmail = email.trim().lowercase()

        // Tài khoản Admin
        if (normalizedEmail == "admin@sis.com" && password == "123456") {
            return "mock_admin_token"
        }

        // Tài khoản Nhân viên
        if (normalizedEmail == "staff@sis.com" && password == "123456") {
            return "mock_staff_token"
        }

        // Thông tin đăng nhập không chính xác
        throw IllegalArgumentException(
            "Email hoặc mật khẩu không đúng."
        )
    }
}
