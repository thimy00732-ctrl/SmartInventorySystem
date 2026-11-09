package com.sis.android.data.model

data class ProductFilter(
    val keyword: String? = null,
    val categoryId: Long? = null,
    val supplierId: Long? = null,
    val isActive: Boolean? = null,
    val sortBy: String = "created_at",
    val sortDirection: String = "DESC"
)
