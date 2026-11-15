package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sis.android.data.model.Product
import com.sis.android.data.model.Category
import com.sis.android.data.model.Supplier
import com.sis.android.data.model.User
import com.sis.android.data.model.ManagedStaff
import com.sis.android.di.AppModule
import com.sis.android.ui.products.BarcodeScannerScreen
import com.sis.android.ui.products.ProductDetailScreen
import com.sis.android.ui.products.ProductFormScreen
import com.sis.android.ui.products.ProductListScreen
import com.sis.android.ui.categories.CategoryListScreen
import com.sis.android.ui.categories.CategoryDetailScreen
import com.sis.android.ui.categories.CategoryFormScreen
import com.sis.android.ui.suppliers.SupplierListScreen
import com.sis.android.ui.suppliers.SupplierDetailScreen
import com.sis.android.ui.suppliers.SupplierFormScreen
import com.sis.android.ui.navigation.AppNavigationDrawer
import com.sis.android.ui.auth.LoginScreen
import com.sis.android.ui.staffmanagement.StaffManagementDetailScreen
import com.sis.android.ui.staffmanagement.StaffManagementFormScreen
import com.sis.android.ui.staffmanagement.StaffManagementListScreen
import com.sis.android.ui.staffmanagement.StaffManagementViewModel
import com.sis.android.ui.theme.SISTheme
import com.sis.android.ui.viewmodel.ProductViewModel
import com.sis.android.ui.viewmodel.CategoryViewModel
import com.sis.android.ui.viewmodel.SupplierViewModel
import com.sis.android.ui.theme.color.CrudColors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SISTheme {
                MainApp()
            }
        }
    }

    @Composable
    private fun MainApp() {
        val sessionManager = remember { AppModule.provideSessionManager(this) }
        val authRepository = remember { AppModule.provideAuthRepository(this) }
        val staffRepository = remember { AppModule.provideStaffRepository() }

        // Kiểm tra session đã lưu khi mở app
        val savedRole = remember { authRepository.getSavedRole() }
        val savedUserId = remember { sessionManager.getUserId() }
        val savedFullName = remember { sessionManager.getFullName() }
        
        var currentUser by remember {
            mutableStateOf<User?>(
                if (savedRole != null && savedUserId > 0) {
                    User(
                        user_id = savedUserId,
                        fullName = savedFullName ?: "User",
                        email = authRepository.getRememberedEmail(),
                        role = savedRole,
                        is_active = true
                    )
                } else {
                    null
                }
            )
        }

        if (currentUser == null) {
            // Chưa đăng nhập → hiện màn hình Login
            LoginScreen(
                onLoginSuccess = { role ->
                    // Tạo User object từ session sau khi login thành công
                    currentUser = User(
                        user_id = sessionManager.getUserId(),
                        fullName = sessionManager.getFullName() ?: "User",
                        email = authRepository.getRememberedEmail(),
                        role = role,
                        is_active = true
                    )
                }
            )
        } else {
            // Đã đăng nhập → hiện màn hình chính với Navigation
            MainScreen(
                currentUser = currentUser!!,
                sessionManager = sessionManager,
                staffRepository = staffRepository
            )
        }
    }
}

/**
 * Bottom Navigation Items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Products : BottomNavItem("LIST", "Sản phẩm", Icons.Default.Inventory)
    object Categories : BottomNavItem("CATEGORIES", "Danh mục", Icons.Default.Category)
    object Suppliers : BottomNavItem("SUPPLIERS", "NCC", Icons.Default.Business)
    object Scanner : BottomNavItem("SCANNER", "Quét", Icons.Default.QrCodeScanner)
}

@Composable
fun MainScreen(
    currentUser: User,
    sessionManager: com.sis.android.data.session.SessionManager,
    staffRepository: com.sis.android.data.repository.StaffManagementRepository
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ViewModels
    val productViewModel: ProductViewModel = viewModel<ProductViewModel>()
    val categoryViewModel: CategoryViewModel = viewModel<CategoryViewModel>()
    val supplierViewModel: SupplierViewModel = viewModel<SupplierViewModel>()
    val staffViewModel: StaffManagementViewModel = viewModel<StaffManagementViewModel>(
        factory = StaffManagementViewModel.Factory(
            repository = staffRepository,
            sessionManager = sessionManager
        )
    )

    // Current screen state
    var currentScreen by remember { mutableStateOf("LIST") }

    // Staff management states
    var selectedStaff by remember { mutableStateOf<ManagedStaff?>(null) }
    var showAddStaffForm by remember { mutableStateOf(false) }
    var showEditStaffForm by remember { mutableStateOf(false) }

    // Bottom nav items
    val bottomNavItems = listOf(
        BottomNavItem.Products,
        BottomNavItem.Categories,
        BottomNavItem.Suppliers,
        BottomNavItem.Scanner
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawer(
                currentUser = currentUser,
                currentRoute = currentScreen,
                onNavigate = { route ->
                    currentScreen = route
                    scope.launch {
                        drawerState.close()
                    }
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                // Chỉ hiện bottom nav khi không ở màn hình Staff Management
                if (!currentScreen.startsWith("STAFF")) {
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = CrudColors.Primary,
                        tonalElevation = 0.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentScreen == item.route ||
                                    (item.route == "LIST" && (currentScreen == "FORM" || currentScreen == "DETAIL"))

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = { Text(item.title) },
                                selected = selected,
                                onClick = {
                                    currentScreen = item.route
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CrudColors.Primary,
                                    selectedTextColor = CrudColors.Primary,
                                    indicatorColor = Color(0xFFE8F5E9),
                                    unselectedIconColor = Color(0xFF374151),
                                    unselectedTextColor = Color(0xFF6B7280)
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = Color.White
            ) {
                // Staff Management Screens
                when {
                    currentScreen == "STAFF_LIST" -> {
                        StaffManagementListScreen(
                            isAdmin = currentUser.role == "ADMIN",
                            viewModel = staffViewModel,
                            onAddStaff = {
                                showAddStaffForm = true
                                currentScreen = "STAFF_FORM_ADD"
                            },
                            onOpenStaff = { staff ->
                                selectedStaff = staff
                                currentScreen = "STAFF_DETAIL"
                            }
                        )
                    }

                    currentScreen == "STAFF_FORM_ADD" -> {
                        val uiState by staffViewModel.uiState.collectAsStateWithLifecycle()
                        StaffManagementFormScreen(
                            staff = null,
                            isLoading = uiState.isLoading,
                            onBack = {
                                currentScreen = "STAFF_LIST"
                                showAddStaffForm = false
                            },
                            onSave = { fullName, email, phone, password, role ->
                                staffViewModel.addStaff(fullName, email, phone, password, role)
                                currentScreen = "STAFF_LIST"
                                showAddStaffForm = false
                            }
                        )
                    }

                    currentScreen == "STAFF_FORM_EDIT" && selectedStaff != null -> {
                        val uiState by staffViewModel.uiState.collectAsStateWithLifecycle()
                        StaffManagementFormScreen(
                            staff = selectedStaff,
                            isLoading = uiState.isLoading,
                            onBack = {
                                currentScreen = "STAFF_DETAIL"
                                showEditStaffForm = false
                            },
                            onSave = { fullName, email, phone, _, role ->
                                val updated = selectedStaff!!.copy(
                                    fullName = fullName,
                                    email = email,
                                    phone = phone,
                                    role = role
                                )
                                staffViewModel.updateStaff(updated)
                                selectedStaff = updated
                                currentScreen = "STAFF_DETAIL"
                                showEditStaffForm = false
                            }
                        )
                    }

                    currentScreen == "STAFF_DETAIL" && selectedStaff != null -> {
                        val uiState by staffViewModel.uiState.collectAsStateWithLifecycle()
                        val latestStaff = uiState.staff.find { it.id == selectedStaff!!.id }
                            ?: selectedStaff!!

                        StaffManagementDetailScreen(
                            staff = latestStaff,
                            isAdmin = currentUser.role == "ADMIN",
                            onBack = {
                                selectedStaff = null
                                currentScreen = "STAFF_LIST"
                            },
                            onEdit = {
                                showEditStaffForm = true
                                currentScreen = "STAFF_FORM_EDIT"
                            },
                            onToggleActive = {
                                staffViewModel.toggleActive(latestStaff)
                            }
                        )
                    }

                    // Products/Categories/Suppliers Screens
                    else -> {
                        AppContent(
                            currentScreen = currentScreen,
                            onScreenChange = { newScreen -> currentScreen = newScreen },
                            productViewModel = productViewModel,
                            categoryViewModel = categoryViewModel,
                            supplierViewModel = supplierViewModel,
                            onMenuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppContent(
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    productViewModel: ProductViewModel,
    categoryViewModel: CategoryViewModel,
    supplierViewModel: SupplierViewModel,
    onMenuClick: () -> Unit
) {
    val categories by categoryViewModel.categoryList.collectAsState()

    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }

    var isSaving by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var notFoundBarcode by remember { mutableStateOf<String?>(null) }

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var supplierToEdit by remember { mutableStateOf<Supplier?>(null) }

    when (currentScreen) {
        "LIST" -> {
            ProductListScreen(
                viewModel = productViewModel,
                onProductClick = { product ->
                    selectedProduct = product
                    onScreenChange("DETAIL")
                },
                onAddClick = {
                    productToEdit = null
                    onScreenChange("FORM")
                },
                onScanClick = {
                    onScreenChange("SCANNER")
                },
                onMenuClick = onMenuClick
            )
        }

        "FORM" -> {
            ProductFormScreen(
                productToEdit = productToEdit,
                categories = categories,
                isSaving = isSaving,
                serverError = formError,
                onSave = { savedProduct ->
                    formError = null
                    if (productToEdit == null) {
                        isSaving = true
                        productViewModel.addProduct(
                            savedProduct,
                            onSuccess = {
                                isSaving = false
                                onScreenChange("LIST")
                            },
                            onError = { errorMsg ->
                                isSaving = false
                                formError = errorMsg
                            }
                        )
                    } else {
                        isSaving = true
                        productViewModel.updateProduct(
                            savedProduct,
                            onSuccess = {
                                isSaving = false
                                onScreenChange("LIST")
                            },
                            onError = { errorMsg ->
                                isSaving = false
                                formError = errorMsg
                            }
                        )
                    }
                },
                onBackClick = {
                    formError = null
                    onScreenChange("LIST")
                }
            )
        }

        "DETAIL" -> {
            selectedProduct?.let { product ->
                ProductDetailScreen(
                    product = product,
                    onBackClick = { onScreenChange("LIST") },
                    onEditClick = { editProd ->
                        productToEdit = editProd
                        onScreenChange("FORM")
                    },
                    onDeleteClick = { deleteProd ->
                        productViewModel.deleteProduct(
                            productId = deleteProd.productId,
                            onSuccess = {
                                onScreenChange("LIST")
                            },
                            onError = { errorMsg ->
                                android.util.Log.e("MainActivity", "Lỗi xóa: $errorMsg")
                            }
                        )
                    },
                    onToggleActiveClick = { toggleProd ->
                        productViewModel.toggleProductActive(
                            product = toggleProd,
                            onSuccess = {
                                selectedProduct = toggleProd.copy(isActive = !toggleProd.isActive)
                            },
                            onError = { errorMsg ->
                                android.util.Log.e("MainActivity", "Error: $errorMsg")
                            }
                        )
                    }
                )
            }
        }

        "SCANNER" -> {
            BarcodeScannerScreen(
                onBarcodeScanned = { code ->
                    productViewModel.scanBarcode(
                        barcode = code,
                        onFound = { product ->
                            selectedProduct = product
                            onScreenChange("DETAIL")
                        },
                        onNotFound = { notFoundCode ->
                            notFoundBarcode = notFoundCode
                        }
                    )
                },
                onBackClick = { onScreenChange("LIST") }
            )
        }

        "CATEGORIES" -> {
            CategoryListScreen(
                viewModel = categoryViewModel,
                onCategoryClick = { category ->
                    selectedCategory = category
                    onScreenChange("CATEGORY_DETAIL")
                },
                onAddClick = {
                    categoryToEdit = null
                    onScreenChange("CATEGORY_FORM")
                },
                onMenuClick = onMenuClick
            )
        }

        "CATEGORY_DETAIL" -> {
            selectedCategory?.let { category ->
                CategoryDetailScreen(
                    category = category,
                    onBackClick = { onScreenChange("CATEGORIES") },
                    onEditClick = { editCat ->
                        categoryToEdit = editCat
                        onScreenChange("CATEGORY_FORM")
                    },
                    onDeleteClick = { deleteCat ->
                        categoryViewModel.deleteCategory(
                            categoryId = deleteCat.categoryId ?: 0L,
                            onSuccess = {
                                selectedCategory = null
                                onScreenChange("CATEGORIES")
                            },
                            onError = { errorMsg ->
                                android.util.Log.e("MainActivity", "Lỗi xóa: $errorMsg")
                            }
                        )
                    },
                    onToggleActiveClick = { toggleCat ->
                        if (toggleCat.isActive) {
                            categoryViewModel.deactivateCategory(toggleCat.categoryId ?: 0L)
                        } else {
                            categoryViewModel.reactivateCategory(toggleCat.categoryId ?: 0L)
                        }
                        categoryViewModel.loadCategories()
                        selectedCategory = toggleCat.copy(isActive = !toggleCat.isActive)
                    }
                )
            }
        }

        "CATEGORY_FORM" -> {
            CategoryFormScreen(
                categoryToEdit = categoryToEdit,
                isSaving = isSaving,
                serverError = formError,
                onSave = { savedCategory ->
                    formError = null
                    if (categoryToEdit == null) {
                        isSaving = true
                        categoryViewModel.addCategory(savedCategory) {
                            isSaving = false
                            onScreenChange("CATEGORIES")
                        }
                    } else {
                        isSaving = true
                        categoryViewModel.updateCategory(savedCategory) {
                            isSaving = false
                            onScreenChange("CATEGORIES")
                        }
                    }
                },
                onBackClick = {
                    formError = null
                    onScreenChange(if (categoryToEdit != null) "CATEGORY_DETAIL" else "CATEGORIES")
                }
            )
        }

        "SUPPLIERS" -> {
            SupplierListScreen(
                viewModel = supplierViewModel,
                onSupplierClick = { supplier ->
                    selectedSupplier = supplier
                    onScreenChange("SUPPLIER_DETAIL")
                },
                onAddClick = {
                    supplierToEdit = null
                    onScreenChange("SUPPLIER_FORM")
                },
                onMenuClick = onMenuClick
            )
        }

        "SUPPLIER_DETAIL" -> {
            selectedSupplier?.let { supplier ->
                SupplierDetailScreen(
                    supplier = supplier,
                    onBackClick = { onScreenChange("SUPPLIERS") },
                    onEditClick = { editSup ->
                        supplierToEdit = editSup
                        onScreenChange("SUPPLIER_FORM")
                    },
                    onDeleteClick = { deleteSup ->
                        supplierViewModel.deleteSupplier(
                            supplierId = deleteSup.supplierId ?: 0L,
                            onSuccess = {
                                selectedSupplier = null
                                onScreenChange("SUPPLIERS")
                            },
                            onError = { errorMsg ->
                                android.util.Log.e("MainActivity", "Lỗi xóa: $errorMsg")
                            }
                        )
                    },
                    onToggleActiveClick = { toggleSup ->
                        if (toggleSup.isActive) {
                            supplierViewModel.deactivateSupplier(toggleSup.supplierId ?: 0L)
                        } else {
                            supplierViewModel.reactivateSupplier(toggleSup.supplierId ?: 0L)
                        }
                        supplierViewModel.loadSuppliers()
                        selectedSupplier = toggleSup.copy(isActive = !toggleSup.isActive)
                    }
                )
            }
        }

        "SUPPLIER_FORM" -> {
            SupplierFormScreen(
                supplierToEdit = supplierToEdit,
                isSaving = isSaving,
                serverError = formError,
                onSave = { savedSupplier ->
                    formError = null
                    if (supplierToEdit == null) {
                        isSaving = true
                        supplierViewModel.addSupplier(savedSupplier) {
                            isSaving = false
                            onScreenChange("SUPPLIERS")
                        }
                    } else {
                        isSaving = true
                        supplierViewModel.updateSupplier(savedSupplier) {
                            isSaving = false
                            onScreenChange("SUPPLIERS")
                        }
                    }
                },
                onBackClick = {
                    formError = null
                    onScreenChange(if (supplierToEdit != null) "SUPPLIER_DETAIL" else "SUPPLIERS")
                }
            )
        }
    }

    notFoundBarcode?.let { code ->
        AlertDialog(
            onDismissRequest = { notFoundBarcode = null },
            title = { Text("Không tìm thấy sản phẩm") },
            text = { Text("Mã vạch hoặc SKU [$code] không tồn tại trong hệ thống kho!") },
            confirmButton = {
                TextButton(onClick = { notFoundBarcode = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}
