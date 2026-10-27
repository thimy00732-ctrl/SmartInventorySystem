
package com.sis.android.data.remote

import kotlinx.coroutines.delay
import java.io.IOException

class adMockAuthApi {

    suspend fun login(
        username: String,
        password: String
    ): String {
        // Gia lap thoi gian phan hoi cua API
        delay(1500)

        // Gia lap loi ket noi mang
        if (username == "offline") {
            throw IOException("Không thể kết nối đến máy chủ")
        }

        // Tai khoan quan tri vien
        if (username == "admin" && password == "123456") {
            return "mock_admin_token"
        }

        // Tai khoan nhan vien
        if (username == "staff" && password == "123456") {
            return "mock_staff_token"
        }

        // Tai khoan hoac mat khau khong dung
        throw IllegalArgumentException(
            "Tên đăng nhập hoặc mật khẩu không đúng."
        )
    }
}
