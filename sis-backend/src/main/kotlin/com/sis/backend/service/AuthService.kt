package com.sis.backend.service

import com.sis.backend.dto.*
import com.sis.backend.entity.RefreshToken
import com.sis.backend.entity.User
import com.sis.backend.repository.RefreshTokenRepository
import com.sis.backend.repository.UserRepository
import com.sis.backend.security.JwtService
import com.sis.backend.security.PasswordService
import java.time.LocalDateTime

/**
 * AuthService: Xử lý tất cả business logic liên quan đến xác thực và quyền
 * - UC01: Đăng nhập
 * - UC02: Đăng xuất
 * - UC03: Quản lý người dùng
 * - UC04: Kiểm soát truy cập
 * - UC05: Quản lý hồ sơ
 * - UC06: Thay đổi mật khẩu
 * - UC07: Làm mới token
 */
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService
) {

    /**
     * UC01: Đăng nhập
     * Kiểm tra email + mật khẩu, tạo JWT tokens nếu hợp lệ
     * @param request - LoginRequest chứa email và password
     * @return LoginResponse chứa user_id, tokens, hoặc exception nếu lỗi
     */
    fun login(request: LoginRequest): LoginResponse {
        // Bước 1: Tìm người dùng theo email
        val user = userRepository.findByEmail(request.email)
            ?: throw Exception("Email hoặc mật khẩu không chính xác")

        // Bước 2: Kiểm tra mật khẩu
        val isPasswordValid = PasswordService.verifyPassword(request.password, user.password_hash!!)
        if (!isPasswordValid) {
            throw Exception("Email hoặc mật khẩu không chính xác")
        }

        // Bước 3: Kiểm tra xem người dùng có cần đổi mật khẩu không
        if (user.must_change_password) {
            throw Exception("Bạn phải thay đổi mật khẩu trước khi đăng nhập")
        }

        // Bước 4: Tạo access token (hợp lệ 1 giờ)
        val accessToken = jwtService.generateAccessToken(user.user_id!!, user.email, user.role)

        // Bước 5: Tạo refresh token (hợp lệ 7 ngày)
        val refreshTokenString = jwtService.generateRefreshToken(user.user_id)
        val refreshTokenEntity = RefreshToken(
            user_id = user.user_id,
            refresh_token = refreshTokenString,
            expires_at = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        // Bước 6: Cập nhật last_login_at
        userRepository.updateLastLogin(user.user_id)

        // Bước 7: Trả về response
        return LoginResponse(
            user_id = user.user_id,
            email = user.email,
            role = user.role,
            access_token = accessToken,
            refresh_token = refreshTokenString,
            expires_in = 3600 // 1 giờ tính bằng giây
        )
    }

    /**
     * UC02: Đăng xuất
     * Thu hồi refresh token để người dùng không thể dùng lại
     * @param request - LogoutRequest chứa refresh_token
     */
    fun logout(request: LogoutRequest) {
        // Bước 1: Tìm refresh token trong database
        val refreshToken = refreshTokenRepository.findByToken(request.refresh_token)
            ?: throw Exception("Refresh token không hợp lệ")

        // Bước 2: Thu hồi token (đặt revoked_at = now)
        val revoked = refreshTokenRepository.revoke(refreshToken.token_id!!)
        if (!revoked) {
            throw Exception("Không thể đăng xuất, vui lòng thử lại")
        }
    }

    /**
     * UC03: Tạo người dùng mới (admin tạo tài khoản cho staff)
     * @param request - CreateUserRequest chứa email, password, role
     * @return UserResponse chứa thông tin người dùng mới
     */
    fun createUser(request: CreateUserRequest): UserResponse {
        // Bước 1: Kiểm tra xem email đã tồn tại chưa
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) {
            throw Exception("Email này đã được sử dụng")
        }

        // Bước 2: Kiểm tra role hợp lệ
        if (request.role !in listOf("ADMIN", "WAREHOUSE_STAFF")) {
            throw Exception("Role không hợp lệ. Phải là: ADMIN hoặc WAREHOUSE_STAFF")
        }

        // Bước 3: Kiểm tra mật khẩu mạnh
        val passwordError = PasswordService.getPasswordErrorMessage(request.password)
        if (passwordError.isNotEmpty()) {
            throw Exception(passwordError)
        }

        // Bước 4: Mã hóa mật khẩu
        val passwordHash = PasswordService.hashPassword(request.password)

        // Bước 5: Tạo user object
        val newUser = User(
            email = request.email,
            password_hash = passwordHash,
            role = request.role,
            status = "ACTIVE"
        )

        // Bước 6: Lưu vào database
        val savedUser = userRepository.save(newUser)

        // Bước 7: Trả về response
        return convertToUserResponse(savedUser)
    }

    /**
     * UC03: Cập nhật thông tin người dùng
     * @param userId - id của người dùng cần cập nhật
     * @param request - UpdateUserRequest chứa email, role, status (tuỳ chọn)
     * @return UserResponse chứa thông tin cập nhật
     */
    fun updateUser(userId: Int, request: UpdateUserRequest): UserResponse {
        // Bước 1: Tìm người dùng
        val user = userRepository.findById(userId)
            ?: throw Exception("Người dùng không tồn tại")

        // Bước 2: Kiểm tra email mới (nếu có)
        if (request.email != null && request.email != user.email) {
            val existingUser = userRepository.findByEmail(request.email)
            if (existingUser != null) {
                throw Exception("Email này đã được sử dụng")
            }
        }

        // Bước 3: Kiểm tra role mới (nếu có)
        if (request.role != null && request.role !in listOf("ADMIN", "WAREHOUSE_STAFF")) {
            throw Exception("Role không hợp lệ")
        }

        // Bước 4: Kiểm tra status mới (nếu có)
        if (request.status != null && request.status !in listOf("ACTIVE", "INACTIVE")) {
            throw Exception("Status không hợp lệ")
        }

        // Bước 5: Cập nhật database
        val updated = userRepository.update(userId, request.email, request.role, request.status)
        if (!updated) {
            throw Exception("Không thể cập nhật người dùng")
        }

        // Bước 6: Lấy user mới nhất
        val updatedUser = userRepository.findById(userId)
            ?: throw Exception("Không thể lấy thông tin người dùng")

        // Bước 7: Trả về response
        return convertToUserResponse(updatedUser)
    }

    /**
     * UC04: Kiểm soát truy cập - xác minh JWT token
     * Hàm này được dùng bởi middleware để kiểm tra tất cả requests
     * @param token - JWT access token
     * @return TokenValidationResponse chứa xem token có hợp lệ không
     */
    fun validateToken(token: String): TokenValidationResponse {
        // Xác minh token
        val claims = jwtService.verifyAccessToken(token)

        return if (claims != null) {
            TokenValidationResponse(
                valid = true,
                user_id = claims.userId,
                email = claims.email,
                role = claims.role,
                message = null
            )
        } else {
            TokenValidationResponse(
                valid = false,
                user_id = null,
                email = null,
                role = null,
                message = "Token không hợp lệ hoặc đã hết hạn"
            )
        }
    }

    /**
     * UC05: Cập nhật hồ sơ người dùng (người dùng cập nhật thông tin của chính mình)
     * @param userId - id của người dùng
     * @param request - UpdateProfileRequest chứa email mới (tuỳ chọn)
     * @return ProfileResponse chứa thông tin hồ sơ cập nhật
     */
    fun updateProfile(userId: Int, request: UpdateProfileRequest): ProfileResponse {
        // Bước 1: Tìm người dùng
        val user = userRepository.findById(userId)
            ?: throw Exception("Người dùng không tồn tại")

        // Bước 2: Kiểm tra email mới (nếu có)
        if (request.email != null && request.email != user.email) {
            val existingUser = userRepository.findByEmail(request.email)
            if (existingUser != null) {
                throw Exception("Email này đã được sử dụng")
            }
        }

        // Bước 3: Cập nhật email (chỉ cập nhật email, không update role/status)
        if (request.email != null) {
            userRepository.update(userId, request.email, null, null)
        }

        // Bước 4: Lấy user mới nhất
        val updatedUser = userRepository.findById(userId)
            ?: throw Exception("Không thể lấy thông tin người dùng")

        // Bước 5: Trả về response
        return convertToProfileResponse(updatedUser)
    }

    /**
     * UC06: Thay đổi mật khẩu
     * @param userId - id của người dùng
     * @param request - ChangePasswordRequest chứa old_password và new_password
     * @return ChangePasswordResponse chứa kết quả
     */
    fun changePassword(userId: Int, request: ChangePasswordRequest): ChangePasswordResponse {
        // Bước 1: Tìm người dùng
        val user = userRepository.findById(userId)
            ?: throw Exception("Người dùng không tồn tại")

        // Bước 2: Kiểm tra mật khẩu cũ
        val isOldPasswordValid = PasswordService.verifyPassword(request.old_password, user.password_hash!!)
        if (!isOldPasswordValid) {
            throw Exception("Mật khẩu cũ không chính xác")
        }

        // Bước 3: Kiểm tra mật khẩu mới có mạnh không
        val passwordError = PasswordService.getPasswordErrorMessage(request.new_password)
        if (passwordError.isNotEmpty()) {
            throw Exception(passwordError)
        }

        // Bước 4: Kiểm tra mật khẩu mới khác mật khẩu cũ
        val isSamePassword = PasswordService.verifyPassword(request.new_password, user.password_hash)
        if (isSamePassword) {
            throw Exception("Mật khẩu mới phải khác mật khẩu cũ")
        }

        // Bước 5: Mã hóa mật khẩu mới
        val newPasswordHash = PasswordService.hashPassword(request.new_password)

        // Bước 6: Cập nhật database
        val updated = userRepository.updatePassword(userId, newPasswordHash)
        if (!updated) {
            throw Exception("Không thể cập nhật mật khẩu")
        }

        // Bước 7: Thu hồi tất cả refresh tokens cũ (buộc người dùng đăng nhập lại)
        refreshTokenRepository.revokeAllByUserId(userId)

        return ChangePasswordResponse(
            message = "Mật khẩu đã được thay đổi thành công",
            success = true
        )
    }

    /**
     * UC07: Làm mới access token bằng refresh token
     * Khi access token hết hạn, dùng refresh token để lấy access token mới
     * @param request - RefreshTokenRequest chứa refresh_token
     * @return RefreshTokenResponse chứa access_token mới
     */
    fun refreshAccessToken(request: RefreshTokenRequest): RefreshTokenResponse {
        // Bước 1: Xác minh refresh token
        val userId = jwtService.verifyRefreshToken(request.refresh_token)
            ?: throw Exception("Refresh token không hợp lệ hoặc đã hết hạn")

        // Bước 2: Tìm refresh token trong database
        val refreshToken = refreshTokenRepository.findByToken(request.refresh_token)
            ?: throw Exception("Refresh token không được tìm thấy")

        // Bước 3: Kiểm tra xem token đã bị thu hồi chưa
        if (refreshToken.revoked_at != null) {
            throw Exception("Refresh token đã bị vô hiệu hóa")
        }

        // Bước 4: Tìm user để lấy email và role
        val user = userRepository.findById(userId)
            ?: throw Exception("Người dùng không tồn tại")

        // Bước 5: Tạo access token mới
        val newAccessToken = jwtService.generateAccessToken(userId, user.email, user.role)

        // Bước 6: Tạo refresh token mới (xoay vòng)
        val newRefreshTokenString = jwtService.generateRefreshToken(userId)
        val newRefreshToken = RefreshToken(
            user_id = userId,
            refresh_token = newRefreshTokenString,
            expires_at = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(newRefreshToken)

        // Bước 7: Thu hồi refresh token cũ
        refreshTokenRepository.revoke(refreshToken.token_id!!)

        return RefreshTokenResponse(
            access_token = newAccessToken,
            refresh_token = newRefreshTokenString,
            expires_in = 3600 // 1 giờ
        )
    }

    /**
     * Helper: Kiểm tra xem user có permission nhất định không
     * Dùng cho role-based access control (UC07)
     * @param userId - id của người dùng
     * @param requiredRole - role cần kiểm tra (ví dụ: "ADMIN")
     * @return true nếu user có role, false nếu không
     */
    fun hasRole(userId: Int, requiredRole: String): Boolean {
        val user = userRepository.findById(userId) ?: return false
        return user.role == requiredRole
    }

    /**
     * Helper: Kiểm tra xem user có role nào trong danh sách không
     * @param userId - id của người dùng
     * @param requiredRoles - list roles cho phép
     * @return true nếu user có 1 trong các roles
     */
    fun hasAnyRole(userId: Int, requiredRoles: List<String>): Boolean {
        val user = userRepository.findById(userId) ?: return false
        return user.role in requiredRoles
    }

    /**
     * Helper: Convert User entity sang UserResponse DTO
     */
    private fun convertToUserResponse(user: User): UserResponse {
        return UserResponse(
            user_id = user.user_id ?: 0,
            email = user.email,
            role = user.role,
            status = user.status,
            last_login_at = user.last_login_at?.toString(),
            created_at = user.created_at?.toString()
        )
    }

    /**
     * Helper: Convert User entity sang ProfileResponse DTO
     */
    private fun convertToProfileResponse(user: User): ProfileResponse {
        return ProfileResponse(
            user_id = user.user_id ?: 0,
            email = user.email,
            role = user.role,
            status = user.status,
            last_login_at = user.last_login_at?.toString(),
            created_at = user.created_at?.toString()
        )
    }
}
