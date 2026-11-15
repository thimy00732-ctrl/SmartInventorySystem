package com.sis.backend.security

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * PasswordService: Quản lý mật khẩu bằng BCrypt
 * - Mã hóa mật khẩu (hash) trước khi lưu vào database
 * - Xác minh mật khẩu nhập vào với password_hash trong database
 * - UC01: Mã hóa mật khẩu khi người dùng đăng nhập
 * - UC06: Mã hóa mật khẩu mới khi thay đổi mật khẩu
 * - UC03: Mã hóa mật khẩu khi tạo người dùng mới
 */
class PasswordService {

    // Chi phí của BCrypt - càng cao càng an toàn nhưng chậm hơn (12 là tiêu chuẩn)
    companion object {
        private const val BCRYPT_COST = 12

        /**
         * UC01/UC03/UC06: Mã hóa mật khẩu bằng BCrypt
         * Tạo hash từ mật khẩu plaintext
         * @param password - mật khẩu plaintext (không mã hóa)
         * @return password_hash - chuỗi hash đã mã hóa (an toàn để lưu vào database)
         *
         * Lưu ý:
         * - Mỗi lần gọi hàm này sẽ tạo ra hash khác nhau (do random salt)
         * - Hash không thể giải mã được, chỉ có thể xác minh
         * - Chi phí 12 mất khoảng 0.1-0.2 giây để tính toán
         */
        fun hashPassword(password: String): String {
            // Tạo random salt và mã hóa mật khẩu
            return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
        }

        /**
         * UC01: Xác minh mật khẩu khi đăng nhập
         * So sánh mật khẩu nhập vào với password_hash trong database
         * @param password - mật khẩu plaintext nhập vào từ người dùng
         * @param passwordHash - password_hash lưu trong database
         * @return true nếu mật khẩu khớp, false nếu không
         *
         * Lưu ý:
         * - Hàm này an toàn chống timing attack (mất time không đổi dù match hay không)
         * - Luôn kiểm tra kỹ chứ không bao giờ cho phép null hoặc empty
         */
        fun verifyPassword(password: String, passwordHash: String): Boolean {
            return try {
                // BCrypt.verifyer sẽ so sánh password với password_hash an toàn
                val result = BCrypt.verifyer().verify(password.toCharArray(), passwordHash)
                result.verified
            } catch (e: Exception) {
                // Nếu lỗi khi kiểm tra (password_hash bị lỗi), trả về false
                false
            }
        }

        /**
         * Helper: Kiểm tra xem mật khẩu có hợp lệ không (độ dài tối thiểu)
         * @param password - mật khẩu cần kiểm tra
         * @return true nếu mật khẩu hợp lệ
         *
         * Yêu cầu:
         * - Tối thiểu 8 ký tự
         * - Bao gồm chữ hoa, chữ thường, số
         * - (Tuỳ chọn: Bao gồm ký tự đặc biệt)
         */
        fun isPasswordStrong(password: String): Boolean {
            // Kiểm tra độ dài
            if (password.length < 8) {
                return false
            }

            // Kiểm tra xem có chữ hoa
            if (!password.any { it.isUpperCase() }) {
                return false
            }

            // Kiểm tra xem có chữ thường
            if (!password.any { it.isLowerCase() }) {
                return false
            }

            // Kiểm tra xem có số
            if (!password.any { it.isDigit() }) {
                return false
            }

            return true
        }

        /**
         * Helper: Lấy error message giải thích tại sao mật khẩu không hợp lệ
         * @param password - mật khẩu cần kiểm tra
         * @return error message hoặc empty string nếu hợp lệ
         */
        fun getPasswordErrorMessage(password: String): String {
            return when {
                password.isEmpty() -> "Mật khẩu không được để trống"
                password.length < 8 -> "Mật khẩu tối thiểu 8 ký tự"
                !password.any { it.isUpperCase() } -> "Mật khẩu phải chứa ít nhất 1 chữ hoa"
                !password.any { it.isLowerCase() } -> "Mật khẩu phải chứa ít nhất 1 chữ thường"
                !password.any { it.isDigit() } -> "Mật khẩu phải chứa ít nhất 1 số"
                else -> ""
            }
        }
    }
}
