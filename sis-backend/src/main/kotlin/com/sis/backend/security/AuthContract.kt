package com.sis.backend.security

/**
 * Contract xác thực JWT - gom config một chỗ.
 * 
 * Người làm JWT cần cài đặt provider với ĐÚNG các giá trị này:
 * - Provider name: AuthContract.PROVIDER
 * - Claim role: AuthContract.ROLE_CLAIM
 * - Claim user ID: AuthContract.USER_ID_CLAIM
 * 
 * KHÔNG thay đổi các hằng số này khi cài JWT thật.
 * KHÔNG cần sửa RoleAuthorization hay route khi JWT được tích hợp.
 */
object AuthContract {
    /**
     * Tên provider JWT trong install(Authentication).
     * 
     * Ví dụ cài đặt (người làm JWT làm):
     * ```
     * install(Authentication) {
     *     jwt(AuthContract.PROVIDER) {
     *         realm = "Smart Inventory System"
     *         verifier(JWTVerifier)
     *         validate { credential ->
     *             val userId = credential.payload.getClaim(AuthContract.USER_ID_CLAIM).asLong()
     *             val role = credential.payload.getClaim(AuthContract.ROLE_CLAIM).asString()
     *             
     *             if (userId != null && role != null) {
     *                 JWTPrincipal(credential.payload)
     *             } else {
     *                 null
     *             }
     *         }
     *     }
     * }
     * ```
     */
    const val PROVIDER = "jwt"
    
    /**
     * Tên claim chứa role trong JWT payload.
     * 
     * Giá trị: "ADMIN" hoặc "WAREHOUSE_STAFF" (string, không phải enum).
     */
    const val ROLE_CLAIM = "role"
    
    /**
     * Tên claim chứa user ID trong JWT payload.
     * 
     * Giá trị: Long (ID người dùng trong bảng users).
     */
    const val USER_ID_CLAIM = "userId"
}
