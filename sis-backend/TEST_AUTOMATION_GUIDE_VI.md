# 📚 Hướng Dẫn Test Automation - Smart Inventory System

**Ngày Tạo:** 2026-11-15  
**Phiên Bản:** 1.0  
**Ngôn Ngữ:** Tiếng Việt

---

## 📋 Mục Lục

1. [Giới Thiệu Chung](#giới-thiệu-chung)
2. [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
3. [Cấu Trúc Test](#cấu-trúc-test)
4. [Hướng Dẫn Chạy Test](#hướng-dẫn-chạy-test)
5. [Cách Viết Test Mới](#cách-viết-test-mới)
6. [Kết Quả Test Hiện Tại](#kết-quả-test-hiện-tại)
7. [Bug Được Phát Hiện](#bug-được-phát-hiện)
8. [Hướng Phát Triển](#hướng-phát-triển)

---

## 🎯 Giới Thiệu Chung

### Mục Đích
Bộ test automation này được viết để:
- ✅ **Kiểm tra logic business** của 4 services chính
- ✅ **Phát hiện bugs** thông qua validation testing
- ✅ **Đảm bảo chất lượng code** trước khi deploy
- ✅ **Tài liệu hóa hành vi hệ thống** (behavior documentation)

### Phạm Vi
Hiện tại test bao phủ **4 services:**

| Service | Test Cases | Status |
|---------|-----------|--------|
| ProductService | 9 | ✅ 8 Pass / ❌ 1 Fail |
| CategoryService | 6 | ✅ 6 Pass |
| SupplierService | 8 | ✅ 8 Pass |
| InventoryService | 10 | ✅ 10 Pass |
| **TOTAL** | **33** | **✅ 32 Pass / ❌ 1 Fail** |

### Tỷ Lệ Thành Công
**97%** - 32/33 tests passed

---

## 🔧 Công Nghệ Sử Dụng

### Framework Test
| Công Nghệ | Mục Đích | Phiên Bản |
|----------|---------|----------|
| **JUnit 5** | Test framework | 5.9.2 |
| **MockK** | Mock repositories | 1.13.5 |
| **Kotlin** | Ngôn ngữ | 1.9.0 |
| **Gradle** | Build tool | 9.3 |
| **Java** | Runtime | 24 |

### Cài Đặt Dependency

File `build.gradle.kts` đã được configure với các dependencies sau:

```kotlin
testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
testImplementation("io.mockk:mockk:1.13.5")
```

---

## 📁 Cấu Trúc Test

### Vị Trí File Test

```
src/test/kotlin/com/sis/backend/
└── ComprehensiveServiceTests.kt (23.2 KB)
```

### Cấu Trúc Bên Trong

```kotlin
class ComprehensiveServiceTests {
    
    @Nested
    inner class ProductServiceTests {
        // 9 test cases
    }
    
    @Nested
    inner class CategoryServiceTests {
        // 6 test cases
    }
    
    @Nested
    inner class SupplierServiceTests {
        // 8 test cases
    }
    
    @Nested
    inner class InventoryServiceTests {
        // 10 test cases
    }
}
```

### Nguyên Tắc Đặt Tên Test

Mỗi test được đặt tên theo format: `[prefixService][number]_test[LogicName]()`

**Ví dụ:**
- `p1_testSalePriceLessThanCost()` - ProductService test 1
- `c2_testWhitespaceCategoryName()` - CategoryService test 2
- `s3_testInvalidPhoneFormat()` - SupplierService test 3
- `i4_testReservedQuantityValidation()` - InventoryService test 4

---

## 🚀 Hướng Dẫn Chạy Test

### 1. Chạy Tất Cả Tests

```bash
cd sis-backend
./gradlew.bat test
```

**Output Mong Đợi:**
```
> Task :test
ComprehensiveServiceTests > ProductServiceTests > p1_testSalePriceLessThanCost FAILED
ComprehensiveServiceTests > ProductServiceTests > p2_testMaxStockLessThanMinStock PASSED
...
32 passed, 1 failed in 3.0s
```

### 2. Chạy Test Cho Một Service Cụ Thể

```bash
# Chỉ test ProductService
./gradlew.bat test --tests "*ProductServiceTests*"

# Chỉ test CategoryService
./gradlew.bat test --tests "*CategoryServiceTests*"
```

### 3. Chạy Test Cụ Thể

```bash
# Chỉ chạy test P-1
./gradlew.bat test --tests "*p1_testSalePriceLessThanCost*"
```

### 4. Chạy Test Với Verbose Output

```bash
./gradlew.bat test --info
```

### 5. Xóa Cache Test Trước

```bash
./gradlew.bat clean test
```

---

## 📝 Cách Viết Test Mới

### Template Cơ Bản

```kotlin
@Test
fun p10_testNewProductValidation() {
    // Arrange (Chuẩn Bị)
    val productRepository = mockk<ProductRepository>()
    val categoryRepository = mockk<CategoryRepository>()
    val supplierRepository = mockk<SupplierRepository>()
    
    val productService = ProductService(
        productRepository,
        categoryRepository,
        supplierRepository
    )
    
    // Act & Assert (Thực Hiện & Kiểm Tra)
    val exception = assertThrows<InvalidProductException> {
        productService.createProduct(
            productName = "",  // Input không hợp lệ
            unit = "cái",
            costPrice = BigDecimal("100"),
            salePrice = BigDecimal("150"),
            minStock = 10,
            maxStock = 100,
            categoryId = 1,
            supplierId = 1
        )
    }
    
    assertEquals(
        "Tên sản phẩm không được để trống.",
        exception.message
    )
}
```

### Cấu Trúc AAA (Arrange-Act-Assert)

1. **Arrange:** Chuẩn bị dữ liệu và objects
2. **Act:** Gọi hàm cần test
3. **Assert:** Kiểm tra kết quả

### Testing Exception

```kotlin
// Kiểm tra exception được throw
assertThrows<InvalidProductException> {
    productService.createProduct(...)
}

// Kiểm tra exception message
val exception = assertThrows<InvalidProductException> {
    productService.createProduct(...)
}
assertEquals("Expected message", exception.message)
```

### Mocking Repository

```kotlin
val repository = mockk<ProductRepository>()

// Mock findById
every { repository.findById(1) } returns Product(...)

// Mock save
every { repository.save(any()) } returns Product(id = 1L, ...)

// Verify called
verify { repository.save(any()) }
```

---

## 📊 Kết Quả Test Hiện Tại

### Tóm Tắt Chung

```
Total Tests:       33
Passed:            32 ✅
Failed:             1 ❌
Success Rate:      97%
Total Duration:    ~3.0 seconds
```

### Breakdown Theo Service

**ProductService:**
- Total: 9 tests
- Passed: 8
- Failed: 1 (P-1)
- Pass Rate: 89%

**CategoryService:**
- Total: 6 tests
- Passed: 6
- Failed: 0
- Pass Rate: 100% ✅

**SupplierService:**
- Total: 8 tests
- Passed: 8
- Failed: 0
- Pass Rate: 100% ✅

**InventoryService:**
- Total: 10 tests
- Passed: 10
- Failed: 0
- Pass Rate: 100% ✅

---

## 🐛 Bug Được Phát Hiện

### Bug #1: CRITICAL - Giá Bán < Giá Vốn Không Được Kiểm Tra

#### Thông Tin Bug

| Trường | Giá Trị |
|--------|--------|
| **ID** | P-1 |
| **Mức Độ** | 🔴 CRITICAL |
| **Status** | ❌ FAILED |
| **Service** | ProductService |
| **Test Method** | `p1_testSalePriceLessThanCost()` |

#### Mô Tả

ProductService không kiểm tra xem salePrice có nhỏ hơn costPrice không. Điều này cho phép tạo sản phẩm với giá bán thấp hơn giá vốn, dẫn đến **lỗ lãi**.

#### Ví Dụ

```kotlin
// Input
costPrice = 150.00
salePrice = 100.00

// Expected: Throw InvalidProductException
// Actual: Tạo sản phẩm thành công ❌
```

#### Tác Hại Kinh Doanh

- 💰 Gây lỗ lãi trực tiếp
- 📉 Sai lệch doanh thu báo cáo
- 🚫 Không tuân thủ business logic

#### Cách Fix

**Tệp:** `src/main/kotlin/com/sis/backend/service/ProductService.kt`

**Phương Thức:** `validateProductData()`

**Thêm Code:**

```kotlin
if (salePrice < costPrice) {
    throw InvalidProductException(
        "Giá bán phải lớn hơn hoặc bằng giá vốn"
    )
}
```

**Vị Trí:** Sau dòng kiểm tra `salePrice < BigDecimal.ZERO`

#### Verify Fix

Sau khi fix, chạy lại test:

```bash
./gradlew.bat test --tests "*p1_testSalePriceLessThanCost*"
```

**Expected:** ✅ PASSED

---

## 🎯 Hướng Phát Triển

### Phase 1: Fix Bug Hiện Tại (Ngay Lập Tức)

- [ ] Fix P-1 bug (salePrice < costPrice)
- [ ] Verify test passes
- [ ] Commit fix

**Ưu Tiên:** 🔴 **CRITICAL**

### Phase 2: Mở Rộng Test Coverage (Tuần Này)

- [ ] Thêm edge case tests
- [ ] Viết integration tests
- [ ] Test API endpoints

**Ưu Tiên:** 🟡 **HIGH**

### Phase 3: Cải Thiện Test Infrastructure (Tháng Này)

- [ ] Tăng code coverage lên >80%
- [ ] Thêm performance tests
- [ ] Setup CI/CD test runner

**Ưu Tiên:** 🟢 **MEDIUM**

### Phase 4: Test Automation Nâng Cao (Tương Lai)

- [ ] End-to-end tests
- [ ] Load testing
- [ ] Security testing

**Ưu Tiên:** 🔵 **LOW**

---

## 📌 Best Practices

### ✅ Làm

- ✅ Viết test trước khi fix bug (TDD)
- ✅ Mỗi test kiểm tra 1 điều cụ thể
- ✅ Đặt tên test rõ ràng, mô tả hành vi
- ✅ Mock external dependencies
- ✅ Chạy test trước khi commit

### ❌ Không Làm

- ❌ Viết test logic = unit logic (2+2=4)
- ❌ Test multiple behaviors trong 1 test
- ❌ Dùng real database trong unit tests
- ❌ Viết test quá phức tạp
- ❌ Ignore failing tests

---

## 📞 Support & Documentation

### Tài Liệu Tham Khảo

- **JUnit 5 Docs:** https://junit.org/junit5/docs/current/user-guide/
- **MockK Docs:** https://mockk.io/
- **Kotlin Testing:** https://kotlinlang.org/docs/testing.html

### Khi Gặp Vấn Đề

1. Kiểm tra log output: `./gradlew.bat test --info`
2. Xóa cache: `./gradlew.bat clean`
3. Rebuild project: `./gradlew.bat build`

### Contact

Liên hệ với team nếu cần hỗ trợ viết test hoặc debug test failures.

---

## 🎓 Tóm Tắt Nhanh

### Chạy Test
```bash
cd sis-backend && ./gradlew.bat test
```

### Fix Bug P-1
1. Mở `ProductService.kt`
2. Tìm `validateProductData()` method
3. Thêm kiểm tra `salePrice >= costPrice`

### Viết Test Mới
1. Copy template từ section "Cách Viết Test Mới"
2. Thay đổi test case logic
3. Chạy test để verify

### Push Code
```bash
git add src/
git commit -m "fix: kiểm tra salePrice >= costPrice"
git push
```

---

**Report được tạo bởi:** Kiro Test Automation  
**Phiên Bản:** 1.0  
**Cập Nhật Lần Cuối:** 2026-11-15
