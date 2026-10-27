
package com.sis.backend.service

import com.sis.backend.dto.CreateCategoryRequest
import com.sis.backend.dto.UpdateCategoryRequest
import com.sis.backend.entity.Category
import com.sis.backend.exception.CategoryInUseException
import com.sis.backend.exception.CategoryNotFoundException
import com.sis.backend.exception.DuplicateCategoryException
import com.sis.backend.exception.InvalidCategoryException
import com.sis.backend.repository.CategoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit test cho CategoryService.
 *
 * Sử dụng MockK để giả lập CategoryRepository,
 * không cần kết nối cơ sở dữ liệu thật.
 */
class CategoryServiceTest {

    /** Repository giả lập phục vụ kiểm thử nghiệp vụ danh mục. */
    private val categoryRepository = mockk<CategoryRepository>()

    /** Service được kiểm thử. */
    private val categoryService = CategoryService(categoryRepository)

    /** Danh mục mẫu đang hoạt động. */
    private val sampleCategory = Category(
        categoryId = 1L,
        categoryName = "Đồ uống",
        description = "Các loại nước uống",
        isActive = true
    )

    /**
     * Kiểm tra lấy danh sách danh mục thành công.
     */
    @Test
    fun `getCategories returns category list`() {
        every {
            categoryRepository.findAll(keyword = null, activeOnly = false)
        } returns listOf(sampleCategory)

        val result = categoryService.getCategories()

        assertEquals(1, result.size)
        assertEquals("Đồ uống", result[0].categoryName)
    }

    /**
     * Kiểm tra tìm danh mục theo ID thành công.
     */
    @Test
    fun `getCategoryById returns category when found`() {
        every {
            categoryRepository.findById(1L)
        } returns sampleCategory

        val result = categoryService.getCategoryById(1L)

        assertEquals(1L, result.categoryId)
        assertEquals("Đồ uống", result.categoryName)
    }

    /**
     * Kiểm tra ID không hợp lệ khi tìm danh mục.
     */
    @Test
    fun `getCategoryById throws exception for invalid ID`() {
        assertFailsWith<InvalidCategoryException> {
            categoryService.getCategoryById(0L)
        }

        verify(exactly = 0) {
            categoryRepository.findById(any())
        }
    }

    /**
     * Kiểm tra không tìm thấy danh mục theo ID.
     */
    @Test
    fun `getCategoryById throws exception when category does not exist`() {
        every {
            categoryRepository.findById(999L)
        } returns null

        assertFailsWith<CategoryNotFoundException> {
            categoryService.getCategoryById(999L)
        }
    }

    /**
     * Kiểm tra tạo danh mục thành công.
     */
    @Test
    fun `createCategory creates category successfully`() {
        val request = CreateCategoryRequest(
            categoryName = "  Đồ uống  ",
            description = "  Các loại nước uống  "
        )

        every {
            categoryRepository.existsByName("Đồ uống")
        } returns false

        every {
            categoryRepository.create(any())
        } answers {
            firstArg<Category>().copy(categoryId = 2L)
        }

        val result = categoryService.createCategory(request)

        assertEquals(2L, result.categoryId)
        assertEquals("Đồ uống", result.categoryName)
        assertEquals("Các loại nước uống", result.description)
        assertTrue(result.isActive)

        verify(exactly = 1) {
            categoryRepository.create(any())
        }
    }

    /**
     * Kiểm tra không cho tạo danh mục có tên trùng.
     */
    @Test
    fun `createCategory throws exception for duplicate name`() {
        val request = CreateCategoryRequest(
            categoryName = "Đồ uống",
            description = null
        )

        every {
            categoryRepository.existsByName("Đồ uống")
        } returns true

        assertFailsWith<DuplicateCategoryException> {
            categoryService.createCategory(request)
        }

        verify(exactly = 0) {
            categoryRepository.create(any())
        }
    }

    /**
     * Kiểm tra không cho tạo danh mục khi tên để trống.
     */
    @Test
    fun `createCategory throws exception for blank name`() {
        val request = CreateCategoryRequest(
            categoryName = "   ",
            description = null
        )

        assertFailsWith<InvalidCategoryException> {
            categoryService.createCategory(request)
        }

        verify(exactly = 0) {
            categoryRepository.create(any())
        }
    }

    /**
     * Kiểm tra không cho tạo danh mục khi tên vượt quá 100 ký tự.
     */
    @Test
    fun `createCategory throws exception for name exceeding limit`() {
        val request = CreateCategoryRequest(
            categoryName = "A".repeat(101),
            description = null
        )

        assertFailsWith<InvalidCategoryException> {
            categoryService.createCategory(request)
        }

        verify(exactly = 0) {
            categoryRepository.create(any())
        }
    }

    /**
     * Kiểm tra cập nhật danh mục thành công.
     */
    @Test
    fun `updateCategory updates category successfully`() {
        val request = UpdateCategoryRequest(
            categoryName = "Thực phẩm",
            description = "Các loại thực phẩm"
        )

        val updatedCategory = sampleCategory.copy(
            categoryName = "Thực phẩm",
            description = "Các loại thực phẩm"
        )

        every {
            categoryRepository.findById(1L)
        } returns sampleCategory

        every {
            categoryRepository.existsByName("Thực phẩm", 1L)
        } returns false

        every {
            categoryRepository.update(
                categoryId = 1L,
                categoryName = "Thực phẩm",
                description = "Các loại thực phẩm"
            )
        } returns updatedCategory

        val result = categoryService.updateCategory(1L, request)

        assertEquals("Thực phẩm", result.categoryName)
        assertEquals("Các loại thực phẩm", result.description)
    }

    /**
     * Kiểm tra cập nhật danh mục không tồn tại.
     */
    @Test
    fun `updateCategory throws exception when category does not exist`() {
        every {
            categoryRepository.findById(999L)
        } returns null

        assertFailsWith<CategoryNotFoundException> {
            categoryService.updateCategory(
                999L,
                UpdateCategoryRequest(
                    categoryName = "Thực phẩm",
                    description = null
                )
            )
        }
    }

    /**
     * Kiểm tra không cho cập nhật tên đã được danh mục khác sử dụng.
     */
    @Test
    fun `updateCategory throws exception for duplicate name`() {
        every {
            categoryRepository.findById(1L)
        } returns sampleCategory

        every {
            categoryRepository.existsByName("Thực phẩm", 1L)
        } returns true

        assertFailsWith<DuplicateCategoryException> {
            categoryService.updateCategory(
                1L,
                UpdateCategoryRequest(
                    categoryName = "Thực phẩm",
                    description = null
                )
            )
        }
    }

    /**
     * Kiểm tra ngừng hoạt động danh mục thành công
     * khi không còn sản phẩm đang hoạt động.
     */
    @Test
    fun `deactivateCategory succeeds when no active products exist`() {
        every {
            categoryRepository.findById(1L)
        } returns sampleCategory

        every {
            categoryRepository.hasActiveProducts(1L)
        } returns false

        every {
            categoryRepository.deactivate(1L)
        } returns true

        val result = categoryService.deactivateCategory(1L)

        assertTrue(result)

        verify(exactly = 1) {
            categoryRepository.deactivate(1L)
        }
    }

    /**
     * Kiểm tra không cho ngừng hoạt động danh mục
     * khi vẫn còn sản phẩm đang hoạt động.
     */
    @Test
    fun `deactivateCategory throws exception when active products exist`() {
        every {
            categoryRepository.findById(1L)
        } returns sampleCategory

        every {
            categoryRepository.hasActiveProducts(1L)
        } returns true

        assertFailsWith<CategoryInUseException> {
            categoryService.deactivateCategory(1L)
        }

        verify(exactly = 0) {
            categoryRepository.deactivate(any())
        }
    }

    /**
     * Kiểm tra không cho ngừng hoạt động danh mục
     * đã ngừng hoạt động trước đó.
     */
    @Test
    fun `deactivateCategory throws exception when category is inactive`() {
        every {
            categoryRepository.findById(1L)
        } returns sampleCategory.copy(isActive = false)

        assertFailsWith<InvalidCategoryException> {
            categoryService.deactivateCategory(1L)
        }

        verify(exactly = 0) {
            categoryRepository.deactivate(any())
        }
    }
}
