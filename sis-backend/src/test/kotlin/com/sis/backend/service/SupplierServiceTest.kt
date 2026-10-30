
package com.sis.backend.service

import com.sis.backend.dto.CreateSupplierRequest
import com.sis.backend.dto.UpdateSupplierRequest
import com.sis.backend.entity.Supplier
import com.sis.backend.exception.DuplicateSupplierException
import com.sis.backend.exception.InvalidSupplierException
import com.sis.backend.exception.SupplierNotFoundException
import com.sis.backend.repository.SupplierRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit Test kiểm thử nghiệp vụ quản lý nhà cung cấp.
 *
 * Sử dụng MockK để giả lập SupplierRepository,
 * không cần kết nối trực tiếp đến cơ sở dữ liệu.
 */
class SupplierServiceTest {

    private lateinit var supplierRepository: SupplierRepository
    private lateinit var supplierService: SupplierService

    private val supplierId = 1L

    /**
     * Khởi tạo Repository giả lập và Service trước mỗi test.
     */
    @BeforeEach
    fun setUp() {
        supplierRepository = mockk(relaxed = true)
        supplierService = SupplierService(supplierRepository)
    }

    /**
     * Tạo dữ liệu nhà cung cấp mẫu.
     */
    private fun createSupplier(
        id: Long = supplierId,
        name: String = "Công ty ABC",
        active: Boolean = true
    ): Supplier {
        val now = LocalDateTime.now()

        return Supplier(
            supplierId = id,
            supplierName = name,
            phone = "0912000010",
            email = "abc@gmail.com",
            address = "Quy Nhơn, Bình Định",
            isActive = active,
            createdAt = now,
            updatedAt = now
        )
    }

    // =====================================================
    // 1. KIỂM THỬ TẠO NHÀ CUNG CẤP
    // =====================================================

    /**
     * SUP_TC01: Tạo nhà cung cấp với dữ liệu hợp lệ.
     */
    @Test
    fun `createSupplier should create supplier successfully`() {
        val request = CreateSupplierRequest(
            supplierName = "Công ty ABC",
            phone = "0912000010",
            email = "abc@gmail.com",
            address = "Quy Nhơn"
        )

        val supplier = createSupplier()

        every {
            supplierRepository.existsByName("Công ty ABC")
        } returns false

        every {
            supplierRepository.create(
                "Công ty ABC",
                "0912000010",
                "abc@gmail.com",
                "Quy Nhơn"
            )
        } returns supplierId

        every {
            supplierRepository.findById(supplierId)
        } returns supplier

        val result = supplierService.createSupplier(request)

        assertEquals("Công ty ABC", result.supplierName)
        assertEquals(supplierId, result.supplierId)

        verify(exactly = 1) {
            supplierRepository.create(
                "Công ty ABC",
                "0912000010",
                "abc@gmail.com",
                "Quy Nhơn"
            )
        }
    }

    /**
     * SUP_TC02: Không cho phép tạo nhà cung cấp trùng tên.
     */
    @Test
    fun `createSupplier should throw exception when name already exists`() {
        val request = CreateSupplierRequest(
            supplierName = "Công ty ABC",
            phone = "0912000010",
            email = "abc@gmail.com",
            address = "Quy Nhơn"
        )

        every {
            supplierRepository.existsByName("Công ty ABC")
        } returns true

        assertFailsWith<DuplicateSupplierException> {
            supplierService.createSupplier(request)
        }

        verify(exactly = 0) {
            supplierRepository.create(any(), any(), any(), any())
        }
    }

    /**
     * SUP_TC03: Không cho phép tên nhà cung cấp để trống.
     */
    @Test
    fun `createSupplier should reject blank supplier name`() {
        val request = CreateSupplierRequest(
            supplierName = "   ",
            phone = "0912000010",
            email = "abc@gmail.com",
            address = "Quy Nhơn"
        )

        assertFailsWith<InvalidSupplierException> {
            supplierService.createSupplier(request)
        }

        verify(exactly = 0) {
            supplierRepository.create(any(), any(), any(), any())
        }
    }

    /**
     * SUP_TC04: Không cho phép email sai định dạng.
     */
    @Test
    fun `createSupplier should reject invalid email`() {
        val request = CreateSupplierRequest(
            supplierName = "Công ty ABC",
            phone = "0912000010",
            email = "abc-email",
            address = "Quy Nhơn"
        )

        assertFailsWith<InvalidSupplierException> {
            supplierService.createSupplier(request)
        }

        verify(exactly = 0) {
            supplierRepository.create(any(), any(), any(), any())
        }
    }

    // =====================================================
    // 2. KIỂM THỬ DANH SÁCH NHÀ CUNG CẤP
    // =====================================================

    /**
     * SUP_TC05: Lấy danh sách nhà cung cấp đang hoạt động.
     */
    @Test
    fun `getSuppliers should return active suppliers`() {
        val suppliers = listOf(
            createSupplier(id = 1L, name = "Công ty ABC"),
            createSupplier(id = 2L, name = "Công ty XYZ")
        )

        every {
            supplierRepository.findAll(null, true)
        } returns suppliers

        val result = supplierService.getSuppliers(
            keyword = null,
            activeOnly = true
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.isActive })

        verify(exactly = 1) {
            supplierRepository.findAll(null, true)
        }
    }

    /**
     * SUP_TC06: Lấy danh sách bao gồm nhà cung cấp ngừng hoạt động.
     */
    @Test
    fun `getSuppliers should return all suppliers when activeOnly is false`() {
        val suppliers = listOf(
            createSupplier(id = 1L, active = true),
            createSupplier(id = 2L, active = false)
        )

        every {
            supplierRepository.findAll(null, false)
        } returns suppliers

        val result = supplierService.getSuppliers(
            keyword = null,
            activeOnly = false
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it.isActive })
        assertTrue(result.any { !it.isActive })
    }

    // =====================================================
    // 3. KIỂM THỬ TÌM KIẾM NHÀ CUNG CẤP
    // =====================================================

    /**
     * SUP_TC07: Tìm kiếm nhà cung cấp theo số điện thoại.
     */
    @Test
    fun `searchSuppliers should find supplier by phone`() {
        val suppliers = listOf(createSupplier())

        every {
            supplierRepository.findAll("0912000010", false)
        } returns suppliers

        val result = supplierService.searchSuppliers("0912000010")

        assertEquals(1, result.size)
        assertEquals("Công ty ABC", result[0].supplierName)

        verify(exactly = 1) {
            supplierRepository.findAll("0912000010", false)
        }
    }

    /**
     * SUP_TC08: Không cho phép tìm kiếm bằng từ khóa trống.
     */
    @Test
    fun `searchSuppliers should reject blank keyword`() {
        assertFailsWith<InvalidSupplierException> {
            supplierService.searchSuppliers("   ")
        }

        verify(exactly = 0) {
            supplierRepository.findAll(any(), any())
        }
    }

    // =====================================================
    // 4. KIỂM THỬ CHI TIẾT NHÀ CUNG CẤP
    // =====================================================

    /**
     * SUP_TC09: Không tìm thấy nhà cung cấp theo ID.
     */
    @Test
    fun `getSupplierDetail should throw exception when supplier not found`() {
        every {
            supplierRepository.findById(supplierId)
        } returns null

        assertFailsWith<SupplierNotFoundException> {
            supplierService.getSupplierDetail(supplierId)
        }
    }

    /**
     * SUP_TC10: Lấy chi tiết nhà cung cấp thành công.
     */
    @Test
    fun `getSupplierDetail should return supplier details`() {
        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier()

        every {
            supplierRepository.findProductsBySupplierId(supplierId)
        } returns emptyList()

        every {
            supplierRepository.findImportTransactionsBySupplierId(supplierId)
        } returns emptyList()

        val result = supplierService.getSupplierDetail(supplierId)

        assertEquals(supplierId, result.supplierId)
        assertEquals("Công ty ABC", result.supplierName)
        assertTrue(result.products.isEmpty())
        assertTrue(result.transactions.isEmpty())
    }

    // =====================================================
    // 5. KIỂM THỬ CẬP NHẬT NHÀ CUNG CẤP
    // =====================================================

    /**
     * SUP_TC11: Cập nhật thông tin nhà cung cấp thành công.
     */
    @Test
    fun `updateSupplier should update supplier successfully`() {
        val request = UpdateSupplierRequest(
            supplierName = "Công ty ABC Mới",
            phone = "0987654321",
            email = "new@gmail.com",
            address = "Đà Nẵng"
        )

        every {
            supplierRepository.findById(supplierId)
        } returnsMany listOf(
            createSupplier(),
            createSupplier(name = "Công ty ABC Mới")
        )

        every {
            supplierRepository.existsByName(
                "Công ty ABC Mới",
                supplierId
            )
        } returns false

        every {
            supplierRepository.update(
                supplierId,
                "Công ty ABC Mới",
                "0987654321",
                "new@gmail.com",
                "Đà Nẵng"
            )
        } returns true

        val result = supplierService.updateSupplier(
            supplierId,
            request
        )

        assertEquals("Công ty ABC Mới", result.supplierName)

        verify(exactly = 1) {
            supplierRepository.update(
                supplierId,
                "Công ty ABC Mới",
                "0987654321",
                "new@gmail.com",
                "Đà Nẵng"
            )
        }
    }

    /**
     * SUP_TC12: Không cập nhật nhà cung cấp không tồn tại.
     */
    @Test
    fun `updateSupplier should throw exception when supplier not found`() {
        val request = UpdateSupplierRequest(
            supplierName = "Công ty ABC Mới",
            phone = "0987654321",
            email = "new@gmail.com",
            address = "Đà Nẵng"
        )

        every {
            supplierRepository.findById(supplierId)
        } returns null

        assertFailsWith<SupplierNotFoundException> {
            supplierService.updateSupplier(supplierId, request)
        }
    }

    /**
     * SUP_TC13: Không cho phép cập nhật thành tên đã thuộc nhà cung cấp khác.
     */
    @Test
    fun `updateSupplier should reject duplicate supplier name`() {
        val request = UpdateSupplierRequest(
            supplierName = "Công ty XYZ",
            phone = "0987654321",
            email = "xyz@gmail.com",
            address = "Đà Nẵng"
        )

        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier()

        every {
            supplierRepository.existsByName("Công ty XYZ", supplierId)
        } returns true

        assertFailsWith<DuplicateSupplierException> {
            supplierService.updateSupplier(supplierId, request)
        }

        verify(exactly = 0) {
            supplierRepository.update(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    // =====================================================
    // 6. KIỂM THỬ NGỪNG HOẠT ĐỘNG
    // =====================================================

    /**
     * SUP_TC14: Ngừng hoạt động nhà cung cấp thành công.
     */
    @Test
    fun `deactivateSupplier should deactivate active supplier`() {
        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier(active = true)

        every {
            supplierRepository.deactivate(supplierId)
        } returns true

        supplierService.deactivateSupplier(supplierId)

        verify(exactly = 1) {
            supplierRepository.deactivate(supplierId)
        }
    }

    /**
     * SUP_TC15: Không cho phép ngừng hoạt động nhà cung cấp đã ngừng.
     */
    @Test
    fun `deactivateSupplier should reject inactive supplier`() {
        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier(active = false)

        assertFailsWith<InvalidSupplierException> {
            supplierService.deactivateSupplier(supplierId)
        }

        verify(exactly = 0) {
            supplierRepository.deactivate(supplierId)
        }
    }

    // =====================================================
    // 7. KIỂM THỬ KÍCH HOẠT LẠI NHÀ CUNG CẤP
    // =====================================================

    /**
     * SUP_TC16: Kích hoạt lại nhà cung cấp đã ngừng hoạt động.
     */
    @Test
    fun `reactivateSupplier should reactivate inactive supplier`() {
        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier(active = false)

        every {
            supplierRepository.reactivate(supplierId)
        } returns true

        supplierService.reactivateSupplier(supplierId)

        verify(exactly = 1) {
            supplierRepository.reactivate(supplierId)
        }
    }

    /**
     * SUP_TC17: Không kích hoạt lại nhà cung cấp đang hoạt động.
     */
    @Test
    fun `reactivateSupplier should reject active supplier`() {
        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier(active = true)

        assertFailsWith<InvalidSupplierException> {
            supplierService.reactivateSupplier(supplierId)
        }

        verify(exactly = 0) {
            supplierRepository.reactivate(supplierId)
        }
    }

    /**
     * SUP_TC18: Xử lý khi Repository không kích hoạt lại thành công.
     */
    @Test
    fun `reactivateSupplier should throw exception when reactivation fails`() {
        every {
            supplierRepository.findById(supplierId)
        } returns createSupplier(active = false)

        every {
            supplierRepository.reactivate(supplierId)
        } returns false

        assertFailsWith<InvalidSupplierException> {
            supplierService.reactivateSupplier(supplierId)
        }
    }
}
