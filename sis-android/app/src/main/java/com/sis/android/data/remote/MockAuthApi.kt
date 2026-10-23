
package com.sis.android.data.remote

import kotlinx.coroutines.delay
import java.io.IOException

class MockAuthApi {

    suspend fun login(
        username: String,
        password: String
    ): Boolean {
        // Gia lap thoi gian phan hoi cua API
        delay(1500)

        // Gia lap loi ket noi mang
        if (username == "offline") {
            throw IOException("Không thể kết nối đến máy chủ")
        }

        // Kiem tra tai khoan mau
        return username == "admin" && password == "123456"
    }
}
