package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("categoryId")
    val categoryId: Long? = null,
    
    @SerializedName("categoryName")
    val categoryName: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)