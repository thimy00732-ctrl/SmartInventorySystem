package com.sis.android.data.model

data class Category(
    val categoryId: Long,
    val categoryName: String,
    val description: String?,
    val status: String // 'ACTIVE', 'INACTIVE'
)