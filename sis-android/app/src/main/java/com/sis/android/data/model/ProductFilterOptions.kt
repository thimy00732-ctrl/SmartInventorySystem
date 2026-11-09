package com.sis.android.data.model

import com.google.gson.annotations.SerializedName

data class ProductFilterOptions(
    @SerializedName("warehouses") val warehouses: List<WarehouseOption> = emptyList(),
    @SerializedName("categories") val categories: List<CategoryOption> = emptyList(),
    @SerializedName("suppliers") val suppliers: List<SupplierOption> = emptyList(),
    @SerializedName("sortByOptions") val sortByOptions: List<ProductSortOption> = emptyList(),
    @SerializedName("sortDirectionOptions") val sortDirectionOptions: List<SortDirectionOption> = emptyList()
)
