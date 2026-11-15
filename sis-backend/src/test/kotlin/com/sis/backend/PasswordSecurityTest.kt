package com.sis.backend

import com.sis.backend.security.PasswordService
import com.sis.backend.security.JwtService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * PasswordSecurityTest: Test password hashing & JWT token
 * Không dùng mock, chỉ test các hàm utility
 */
class PasswordSecurityTest {

    /**
     * Test 1: Mã hóa mật khẩu BCrypt thành công
     */
    @Test
    fun testPasswordHashingSuccess() {
        // Arrange
        val plainPassword = "Password123"

        // Act
        val hashedPassword = PasswordService.hashPassword(plainPassword)

        // Assert
        assertNotNull(hashedPassword)
        assertEquals(true, hashedPassword.isNotEmpty())  // BCrypt hash không trống
        assertEquals(true, hashedPassword.length > 10)   // BCrypt hash >= 60 ký tự
    }

    /**
     * Test 2: Xác minh mật khẩu đúng
     */
    @Test
    fun testPasswordVerificationCorrect() {
        // Arrange
        val plainPassword = "Password123"
        val hashedPassword = PasswordService.hashPassword(plainPassword)

        // Act
        val isValid = PasswordService.verifyPassword(plainPassword, hashedPassword)

        // Assert
        assertEquals(true, isValid)
    }

    /**
     * Test 3: Xác minh mật khẩu sai
     */
    @Test
    fun testPasswordVerificationWrong() {
        // Arrange
        val plainPassword = "Password123"
        val wrongPassword = "WrongPassword"
        val hashedPassword = PasswordService.hashPassword(plainPassword)

        // Act
        val isValid = PasswordService.verifyPassword(wrongPassword, hashedPassword)

        // Assert
        assertEquals(false, isValid)
    }

    /**
     * Test 4: Mật khẩu mạnh - Hợp lệ
     */
    @Test
    fun testStrongPasswordValid() {
        assertEquals(true, PasswordService.isPasswordStrong("Password123"))
        assertEquals(true, PasswordService.isPasswordStrong("SecurePass456"))
    }

    /**
     * Test 5: Mật khẩu yếu - Quá ngắn
     */
    @Test
    fun testStrongPasswordTooShort() {
        assertEquals(false, PasswordService.isPasswordStrong("Pass12"))
    }

    /**
     * Test 6: Mật khẩu yếu - Không có chữ hoa
     */
    @Test
    fun testStrongPasswordNoUppercase() {
        assertEquals(false, PasswordService.isPasswordStrong("password123"))
    }

    /**
     * Test 7: Mật khẩu yếu - Không có chữ thường
     */
    @Test
    fun testStrongPasswordNoLowercase() {
        assertEquals(false, PasswordService.isPasswordStrong("PASSWORD123"))
    }

    /**
     * Test 8: Mật khẩu yếu - Không có số
     */
    @Test
    fun testStrongPasswordNoNumber() {
        assertEquals(false, PasswordService.isPasswordStrong("PasswordAbc"))
    }

    /**
     * Test 9: Error message - Mật khẩu trống
     */
    @Test
    fun testPasswordErrorEmpty() {
        val error = PasswordService.getPasswordErrorMessage("")
        assertNotNull(error)
        assertEquals(true, error.isNotEmpty())
    }

    /**
     * Test 10: Error message - Mật khẩu quá ngắn
     */
    @Test
    fun testPasswordErrorTooShort() {
        val error = PasswordService.getPasswordErrorMessage("Pass1")
        assertNotNull(error)
        assertEquals(true, error.isNotEmpty())
    }

    /**
     * Test 11: Error message - Mật khẩu hợp lệ
     */
    @Test
    fun testPasswordErrorValid() {
        val error = PasswordService.getPasswordErrorMessage("Password123")
        assertEquals("", error)
    }
}

/**
 * LoginSecurityTest: Test login với plaintext password (sẽ fail vì password trong DB phải mã hóa)
 */
class LoginSecurityTest {

    /**
     * Test: Login thất bại khi so sánh plaintext password với BCrypt hash
     * 
     * Scenario: Nếu password trong database là plaintext (không mã hóa),
     * login sẽ thất bại vì:
     * - request.password = "Password123" (plaintext)
     * - user.password_hash = "$2b$12$..." (BCrypt hash)
     * - PasswordService.verifyPassword() không thể so sánh được
     */
    @Test
    fun testLoginFailsWithPlaintextPasswordComparison() {
        // Arrange
        val plainPassword = "Password123"
        val hashedPassword = PasswordService.hashPassword(plainPassword)
        
        // Giả sử database lưu plaintext (BUG!)
        val plaintextPasswordInDB = plainPassword
        
        // Act - Cố gắng xác minh plaintext với plaintext (không phải hash)
        val isValidWithPlaintext = PasswordService.verifyPassword(
            plainPassword, 
            plaintextPasswordInDB
        )
        
        // Assert - Sẽ fail vì BCrypt không thể xác minh plaintext
        assertEquals(false, isValidWithPlaintext)
    }

    /**
     * Test: Login thành công khi password được mã hóa đúng cách
     * 
     * Scenario: Password đúng trong database phải là BCrypt hash
     */
    @Test
    fun testLoginSuccessWithProperBCryptHash() {
        // Arrange
        val plainPassword = "Password123"
        val hashedPassword = PasswordService.hashPassword(plainPassword)
        
        // Act - Xác minh plaintext với BCrypt hash (ĐÚNG)
        val isValid = PasswordService.verifyPassword(plainPassword, hashedPassword)
        
        // Assert
        assertEquals(true, isValid)
    }

    /**
     * Test: Hiện tại database có plaintext → Login FAIL
     * 
     * ⚠️ BUG REPORT:
     * Các user trong database có password_hash = plaintext string
     * Ví dụ: password_hash = "Password123" (thay vì "$2b$12$...")
     * 
     * Kết quả: 
     * - PasswordService.verifyPassword("Password123", "Password123") = FALSE
     * - Vì BCrypt không thể xác minh plaintext
     * - Người dùng không đăng nhập được
     */
    @Test
    fun testCurrentDatabaseBugPlaintextPassword() {
        // Arrange - Mô phỏng mật khẩu plaintext trong database (BUG hiện tại)
        val plaintextPassword = "Password123"
        
        // Act - Cố gắng xác minh
        val loginResult = PasswordService.verifyPassword(plaintextPassword, plaintextPassword)
        
        // Assert - FAIL ❌
        // Vì BCrypt không thể xác minh plaintext string với plaintext string
        assertEquals(false, loginResult)
        
        // Kết luận: Database cần update password thành BCrypt hash!
    }

    /**
     * Test: Cách FIX - Cập nhật database password thành BCrypt
     * 
     * SQL Command cần chạy:
     * UPDATE users SET password_hash = '$2b$12$[HASH]' WHERE email = 'admin@example.com';
     */
    @Test
    fun testFixRequiredUpdateDatabasePasswords() {
        // Arrange
        val plaintextPassword = "Password123"
        
        // Cách làm ĐÚNG: Hash password trước lưu vào database
        val correctHash = PasswordService.hashPassword(plaintextPassword)
        
        // Act
        val loginWithCorrectHash = PasswordService.verifyPassword(plaintextPassword, correctHash)
        
        // Assert - ✅ PASS
        assertEquals(true, loginWithCorrectHash)
        
        // Kết luận:
        // 1. Update tất cả password trong database thành BCrypt hash
        // 2. AuthService đã fix để dùng PasswordService.verifyPassword()
        // 3. Sau đó login sẽ hoạt động bình thường
    }
}

/**
 * JWTTokenTest: Test JWT token generation & verification
 */
class JWTTokenTest {

    private val jwtService = JwtService()

    /**
     * Test 1: Tạo access token
     */
    @Test
    fun testGenerateAccessToken() {
        // Act
        val token = jwtService.generateAccessToken(1, "user@example.com", "ADMIN")

        // Assert
        assertNotNull(token)
        assertEquals(true, token.contains("."))  // JWT có 3 phần ngăn bằng dấu chấm
    }

    /**
     * Test 2: Xác minh access token đúng
     */
    @Test
    fun testVerifyAccessTokenCorrect() {
        // Arrange
        val token = jwtService.generateAccessToken(1, "user@example.com", "ADMIN")

        // Act
        val claims = jwtService.verifyAccessToken(token)

        // Assert
        assertNotNull(claims)
        assertEquals(1, claims.userId)
        assertEquals("user@example.com", claims.email)
        assertEquals("ADMIN", claims.role)
    }

    /**
     * Test 3: Xác minh access token không hợp lệ
     */
    @Test
    fun testVerifyAccessTokenInvalid() {
        // Act
        val claims = jwtService.verifyAccessToken("invalid.token.here")

        // Assert
        assertEquals(null, claims)
    }

    /**
     * Test 4: Tạo refresh token
     */
    @Test
    fun testGenerateRefreshToken() {
        // Act
        val token = jwtService.generateRefreshToken(1)

        // Assert
        assertNotNull(token)
        assertEquals(true, token.contains("."))
    }

    /**
     * Test 5: Xác minh refresh token đúng
     */
    @Test
    fun testVerifyRefreshTokenCorrect() {
        // Arrange
        val token = jwtService.generateRefreshToken(1)

        // Act
        val userId = jwtService.verifyRefreshToken(token)

        // Assert
        assertEquals(1, userId)
    }

    /**
     * Test 6: Xác minh refresh token không hợp lệ
     */
    @Test
    fun testVerifyRefreshTokenInvalid() {
        // Act
        val userId = jwtService.verifyRefreshToken("invalid.token.here")

        // Assert
        assertEquals(null, userId)
    }

    /**
     * Test 7: Trích xuất user ID từ token
     */
    @Test
    fun testExtractUserIdFromToken() {
        // Arrange
        val token = jwtService.generateAccessToken(1, "user@example.com", "ADMIN")

        // Act
        val userId = jwtService.getUserIdFromToken(token)

        // Assert
        assertEquals(1, userId)
    }

    /**
     * Test 8: Access token chứa tất cả claims
     */
    @Test
    fun testAccessTokenContainsAllClaims() {
        // Arrange
        val token = jwtService.generateAccessToken(1, "admin@example.com", "ADMIN")

        // Act
        val claims = jwtService.verifyAccessToken(token)

        // Assert
        assertNotNull(claims)
        assertEquals(1, claims.userId)
        assertEquals("admin@example.com", claims.email)
        assertEquals("ADMIN", claims.role)
        assertNotNull(claims.expiresAt)
    }
}
