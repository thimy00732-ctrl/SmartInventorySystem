// Test BCrypt hash
// Compile: kotlinc test-hash.kt -include-runtime -d test-hash.jar
// Run: java -jar test-hash.jar

fun main() {
    val password = "Admin@123"
    
    // Test hash from SQL
    val hash1 = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.IcKr/Yh2vC6q6Xz2ixW8v/F2.c6KBm"
    
    println("Testing password: $password")
    println("With hash: ${hash1.take(30)}...")
    
    // Trong thực tế bạn cần BCrypt library để verify
    // Nhưng đây là test script đơn giản
    
    println("\nPlease use online tool to verify:")
    println("1. Go to: https://bcrypt-generator.com/")
    println("2. Enter password: $password")
    println("3. Cost: 10")
    println("4. Click 'Hash'")
    println("5. Copy the hash and UPDATE to database")
    println("\nOR use: https://www.bcrypt.online/ to VERIFY existing hash")
}
