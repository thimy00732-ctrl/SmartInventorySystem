package com.sis.backend.routes

import com.sis.backend.dto.*
import com.sis.backend.repository.RefreshTokenRepository
import com.sis.backend.repository.UserRepository
import com.sis.backend.security.JwtService
import com.sis.backend.service.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javax.sql.DataSource

/**
 * AuthRoutes: Định nghĩa tất cả API endpoints liên quan đến authentication & authorization
 * - UC01: POST /api/auth/login
 * - UC02: POST /api/auth/logout
 * - UC03: POST /api/auth/users, PUT /api/auth/users/{id}
 * - UC04: GET /api/auth/validate (middleware check)
 * - UC05: PUT /api/auth/profile
 * - UC06: POST /api/auth/change-password
 * - UC07: POST /api/auth/refresh-token
 */
fun Route.authRoutes(dataSource: DataSource) {
    // Khởi tạo repositories
    val userRepository = UserRepository(dataSource)
    val refreshTokenRepository = RefreshTokenRepository(dataSource)

    // Khởi tạo services
    val jwtService = JwtService()
    val authService = AuthService(userRepository, refreshTokenRepository, jwtService)

    route("/api/auth") {

        /**
         * UC01: Đăng nhập
         * POST /api/auth/login
         *
         * Request body:
         * {
         *   "email": "user@example.com",
         *   "password": "Password123"
         * }
         *
         * Response (200 OK):
         * {
         *   "user_id": 1,
         *   "email": "user@example.com",
         *   "role": "ADMIN",
         *   "access_token": "eyJhbGc...",
         *   "refresh_token": "eyJhbGc...",
         *   "expires_in": 3600
         * }
         *
         * Error (400/401):
         * {
         *   "error": "Email hoặc mật khẩu không chính xác"
         * }
         */
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                // Kiểm tra input
                if (request.email.isBlank() || request.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Email và mật khẩu không được để trống")
                    )
                    return@post
                }

                // Gọi auth service
                val response = authService.login(request)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to (e.message ?: "Lỗi đăng nhập"))
                )
            }
        }

        /**
         * UC02: Đăng xuất
         * POST /api/auth/logout
         *
         * Request body:
         * {
         *   "refresh_token": "eyJhbGc..."
         * }
         *
         * Response (200 OK):
         * {
         *   "message": "Đăng xuất thành công"
         * }
         *
         * Error (401):
         * {
         *   "error": "Refresh token không hợp lệ"
         * }
         */
        post("/logout") {
            try {
                val request = call.receive<LogoutRequest>()

                // Kiểm tra input
                if (request.refresh_token.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Refresh token không được để trống")
                    )
                    return@post
                }

                // Gọi auth service
                authService.logout(request)
                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Đăng xuất thành công")
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to (e.message ?: "Lỗi đăng xuất"))
                )
            }
        }

        /**
         * UC03: Tạo người dùng mới (chỉ admin)
         * POST /api/auth/users
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Request body:
         * {
         *   "email": "staff@example.com",
         *   "password": "Password123",
         *   "role": "WAREHOUSE_STAFF"
         * }
         *
         * Response (201 Created):
         * {
         *   "user_id": 2,
         *   "email": "staff@example.com",
         *   "role": "WAREHOUSE_STAFF",
         *   "status": "ACTIVE",
         *   "last_login_at": null,
         *   "created_at": "2024-01-15T10:30:00"
         * }
         *
         * Error (400/401):
         * {
         *   "error": "Email này đã được sử dụng"
         * }
         */
        post("/users") {
            try {
                // Kiểm tra JWT token (UC04 - Access Control)
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    return@post
                }

                val token = authHeader.removePrefix("Bearer ")
                val claims = jwtService.verifyAccessToken(token)
                if (claims == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Token không hợp lệ hoặc đã hết hạn")
                    )
                    return@post
                }

                // Kiểm tra xem user có phải ADMIN không (chỉ admin mới tạo user)
                if (claims.role != "ADMIN") {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Chỉ admin mới có quyền tạo người dùng")
                    )
                    return@post
                }

                // Nhận request
                val request = call.receive<CreateUserRequest>()

                // Kiểm tra input
                if (request.email.isBlank() || request.password.isBlank() || request.role.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Email, mật khẩu, role không được để trống")
                    )
                    return@post
                }

                // Gọi auth service
                val response = authService.createUser(request)
                call.respond(HttpStatusCode.Created, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Lỗi tạo người dùng"))
                )
            }
        }

        /**
         * UC03: Cập nhật thông tin người dùng (chỉ admin)
         * PUT /api/auth/users/{id}
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Request body:
         * {
         *   "email": "new-email@example.com",
         *   "role": "ADMIN",
         *   "status": "INACTIVE"
         * }
         *
         * Response (200 OK):
         * {
         *   "user_id": 2,
         *   "email": "new-email@example.com",
         *   "role": "ADMIN",
         *   "status": "INACTIVE",
         *   "last_login_at": "2024-01-15T10:30:00",
         *   "created_at": "2024-01-15T10:30:00"
         * }
         */
        put("/users/{id}") {
            try {
                // Kiểm tra JWT token
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    return@put
                }

                val token = authHeader.removePrefix("Bearer ")
                val claims = jwtService.verifyAccessToken(token)
                if (claims == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Token không hợp lệ hoặc đã hết hạn")
                    )
                    return@put
                }

                // Kiểm tra xem user có phải ADMIN không
                if (claims.role != "ADMIN") {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Chỉ admin mới có quyền cập nhật người dùng")
                    )
                    return@put
                }

                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "User ID không hợp lệ")
                    )

                val request = call.receive<UpdateUserRequest>()

                // Gọi auth service
                val response = authService.updateUser(userId, request)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Lỗi cập nhật người dùng"))
                )
            }
        }

        /**
         * UC04: Xác minh token (dùng bởi middleware)
         * GET /api/auth/validate
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Response (200 OK):
         * {
         *   "valid": true,
         *   "user_id": 1,
         *   "email": "user@example.com",
         *   "role": "ADMIN",
         *   "message": null
         * }
         *
         * Response (401):
         * {
         *   "valid": false,
         *   "user_id": null,
         *   "email": null,
         *   "role": null,
         *   "message": "Token không hợp lệ hoặc đã hết hạn"
         * }
         */
        get("/validate") {
            val authHeader = call.request.headers["Authorization"]
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                val response = TokenValidationResponse(
                    valid = false,
                    user_id = null,
                    email = null,
                    role = null,
                    message = "Thiếu authorization header"
                )
                call.respond(HttpStatusCode.Unauthorized, response)
                return@get
            }

            val token = authHeader.removePrefix("Bearer ")
            val validationResponse = authService.validateToken(token)

            val statusCode = if (validationResponse.valid) HttpStatusCode.OK else HttpStatusCode.Unauthorized
            call.respond(statusCode, validationResponse)
        }

        /**
         * UC05: Cập nhật hồ sơ người dùng (người dùng cập nhật info của chính mình)
         * PUT /api/auth/profile
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Request body:
         * {
         *   "email": "new-email@example.com"
         * }
         *
         * Response (200 OK):
         * {
         *   "user_id": 1,
         *   "email": "new-email@example.com",
         *   "role": "ADMIN",
         *   "status": "ACTIVE",
         *   "last_login_at": "2024-01-15T10:30:00",
         *   "created_at": "2024-01-15T10:30:00"
         * }
         */
        put("/profile") {
            try {
                // Kiểm tra JWT token
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    return@put
                }

                val token = authHeader.removePrefix("Bearer ")
                val claims = jwtService.verifyAccessToken(token)
                    ?: return@put call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Token không hợp lệ hoặc đã hết hạn")
                    )

                val request = call.receive<UpdateProfileRequest>()

                // Gọi auth service với user_id từ claims
                val response = authService.updateProfile(claims.userId, request)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Lỗi cập nhật hồ sơ"))
                )
            }
        }

        /**
         * UC06: Thay đổi mật khẩu
         * POST /api/auth/change-password
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Request body:
         * {
         *   "old_password": "OldPassword123",
         *   "new_password": "NewPassword456"
         * }
         *
         * Response (200 OK):
         * {
         *   "message": "Mật khẩu đã được thay đổi thành công",
         *   "success": true
         * }
         *
         * Error (400):
         * {
         *   "error": "Mật khẩu cũ không chính xác"
         * }
         */
        post("/change-password") {
            try {
                // Kiểm tra JWT token
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    return@post
                }

                val token = authHeader.removePrefix("Bearer ")
                val claims = jwtService.verifyAccessToken(token)
                    ?: return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Token không hợp lệ hoặc đã hết hạn")
                    )

                val request = call.receive<ChangePasswordRequest>()

                // Kiểm tra input
                if (request.old_password.isBlank() || request.new_password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Mật khẩu cũ và mới không được để trống")
                    )
                    return@post
                }

                // Gọi auth service
                val response = authService.changePassword(claims.userId, request)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Lỗi thay đổi mật khẩu"))
                )
            }
        }

        /**
         * UC07: Làm mới access token bằng refresh token
         * POST /api/auth/refresh-token
         *
         * Request body:
         * {
         *   "refresh_token": "eyJhbGc..."
         * }
         *
         * Response (200 OK):
         * {
         *   "access_token": "eyJhbGc...",
         *   "refresh_token": "eyJhbGc...",
         *   "expires_in": 3600
         * }
         *
         * Error (401):
         * {
         *   "error": "Refresh token không hợp lệ hoặc đã hết hạn"
         * }
         */
        post("/refresh-token") {
            try {
                val request = call.receive<RefreshTokenRequest>()

                // Kiểm tra input
                if (request.refresh_token.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Refresh token không được để trống")
                    )
                    return@post
                }

                // Gọi auth service
                val response = authService.refreshAccessToken(request)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to (e.message ?: "Lỗi làm mới token"))
                )
            }
        }

        /**
         * UC08 (NEW): Lấy chi tiết 1 staff theo ID
         * GET /api/auth/users/{id}
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Response (200 OK):
         * {
         *   "user_id": 2,
         *   "email": "staff@example.com",
         *   "full_name": "John Doe",
         *   "phone": "0123456789",
         *   "role": "WAREHOUSE_STAFF",
         *   "status": "ACTIVE",
         *   "last_login_at": "2024-01-15T10:30:00",
         *   "created_at": "2024-01-15T10:30:00"
         * }
         *
         * Error (401):
         * {
         *   "error": "Token không hợp lệ hoặc đã hết hạn"
         * }
         *
         * Error (404):
         * {
         *   "error": "Nhân viên không tồn tại"
         * }
         */
        get("/users/{id}") {
            try {
                // Kiểm tra JWT token
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    return@get
                }

                val token = authHeader.removePrefix("Bearer ")
                val claims = jwtService.verifyAccessToken(token)
                if (claims == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Token không hợp lệ hoặc đã hết hạn")
                    )
                    return@get
                }

                val userId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "User ID không hợp lệ")
                    )

                // Gọi auth service
                val response = authService.getStaffById(userId)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to (e.message ?: "Nhân viên không tồn tại"))
                )
            }
        }

        /**
         * UC09 (NEW): Lấy danh sách tất cả staff
         * GET /api/auth/users?limit=50&offset=0
         *
         * Headers:
         * Authorization: Bearer <access_token>
         *
         * Query parameters (tuỳ chọn):
         * - limit: số lượng staff mỗi trang (mặc định 50)
         * - offset: vị trí bắt đầu (mặc định 0)
         *
         * Response (200 OK):
         * {
         *   "total": 5,
         *   "staff": [
         *     {
         *       "user_id": 1,
         *       "email": "admin@example.com",
         *       "full_name": "Admin User",
         *       "phone": "0123456789",
         *       "role": "ADMIN",
         *       "status": "ACTIVE",
         *       "last_login_at": "2024-01-15T10:30:00",
         *       "created_at": "2024-01-15T10:30:00"
         *     },
         *     {
         *       "user_id": 2,
         *       "email": "staff@example.com",
         *       "full_name": "Staff User",
         *       "phone": "0987654321",
         *       "role": "WAREHOUSE_STAFF",
         *       "status": "ACTIVE",
         *       "last_login_at": null,
         *       "created_at": "2024-01-15T10:31:00"
         *     }
         *   ]
         * }
         *
         * Error (401):
         * {
         *   "error": "Token không hợp lệ hoặc đã hết hạn"
         * }
         */
        get("/users") {
            try {
                // Kiểm tra JWT token
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Thiếu hoặc token không hợp lệ")
                    )
                    return@get
                }

                val token = authHeader.removePrefix("Bearer ")
                val claims = jwtService.verifyAccessToken(token)
                if (claims == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Token không hợp lệ hoặc đã hết hạn")
                    )
                    return@get
                }

                // Kiểm tra xem user có phải ADMIN không (chỉ admin mới xem danh sách)
                if (claims.role != "ADMIN") {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Chỉ admin mới có quyền xem danh sách nhân viên")
                    )
                    return@get
                }

                // Lấy query parameters
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                // Kiểm tra giá trị hợp lệ
                if (limit < 1 || limit > 1000) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Limit phải từ 1 đến 1000")
                    )
                    return@get
                }

                if (offset < 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Offset không được âm")
                    )
                    return@get
                }

                // Gọi auth service
                val response = authService.getAllStaff(limit, offset)
                call.respond(HttpStatusCode.OK, response)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Lỗi lấy danh sách nhân viên"))
                )
            }
        }
    }
}
