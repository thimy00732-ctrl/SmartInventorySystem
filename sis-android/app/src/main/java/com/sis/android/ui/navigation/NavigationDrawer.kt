package com.sis.android.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sis.android.data.model.User
import com.sis.android.ui.theme.color.CrudColors

/**
 * Navigation Drawer với expandable sections
 * Thiết kế dựa trên 111 UC functions từ tài liệu yêu cầu
 */

// Data models cho navigation items
data class NavigationItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val badge: BadgeInfo? = null,
    val children: List<NavigationChild>? = null,
    val requiredRoles: List<String>? = null // null = all roles
)

data class NavigationChild(
    val id: String,
    val title: String,
    val route: String,
    val badge: BadgeInfo? = null
)

data class BadgeInfo(
    val count: Int? = null,
    val text: String? = null,
    val color: Color = Color(0xFFE53935) // Red for warnings
)

@Composable
fun AppNavigationDrawer(
    currentUser: User?,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = remember(currentUser) {
        buildNavigationItems(currentUser)
    }

    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight(),
        drawerContainerColor = Color(0xFFFAFAFA) // Light gray background
    ) {
        // Header - User info
        DrawerHeader(currentUser = currentUser)
        
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        
        // Navigation items
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Thay fillMaxSize() bằng weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(navigationItems) { item ->
                if (item.children != null) {
                    // Expandable section
                    ExpandableNavigationItem(
                        item = item,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            onNavigate(route)
                            onCloseDrawer()
                        }
                    )
                } else {
                    // Direct navigation item
                    DirectNavigationItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            item.route?.let {
                                onNavigate(it)
                                onCloseDrawer()
                            }
                        }
                    )
                }
            }
        }
        
        // Logout button
        LogoutButton(onLogout = onLogout)
        
        // Footer - Version info
        DrawerFooter()
    }
}

@Composable
private fun DrawerHeader(currentUser: User?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CrudColors.HeaderBrush) // Gradient xanh olive
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color(0xFF1E4620) // Xanh đậm hơn
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.fullName?.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column {
                Text(
                    text = currentUser?.fullName ?: "Guest User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Text(
                    text = when (currentUser?.role) {
                        "ADMIN" -> "Quản trị viên"
                        "WAREHOUSE_MANAGER" -> "Quản lý kho"
                        "WAREHOUSE_STAFF" -> "Nhân viên kho"
                        else -> "Chưa đăng nhập"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun ExpandableNavigationItem(
    item: NavigationItem,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Section header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = if (isExpanded) {
                Color(0xFFE8F5E9) // Light green background
            } else {
                Color.Transparent
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp),
                    tint = if (isExpanded) {
                        CrudColors.Primary // Xanh olive
                    } else {
                        Color(0xFF6B7280) // Gray
                    }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isExpanded) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isExpanded) {
                        CrudColors.Primary // Xanh olive
                    } else {
                        CrudColors.TextPrimary // Dark gray
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Badge (nếu có)
                item.badge?.let { badge ->
                    BadgeDisplay(badge = badge)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Expand/Collapse arrow
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Children items với animation
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 4.dp)
            ) {
                item.children?.forEach { child ->
                    ChildNavigationItem(
                        child = child,
                        isSelected = currentRoute == child.route,
                        onClick = { onNavigate(child.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectNavigationItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            Color(0xFFE8F5E9) // Light green
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) {
                    CrudColors.Primary
                } else {
                    Color(0xFF6B7280)
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) {
                    CrudColors.Primary
                } else {
                    CrudColors.TextPrimary
                },
                modifier = Modifier.weight(1f)
            )
            
            item.badge?.let { badge ->
                BadgeDisplay(badge = badge)
            }
        }
    }
}

@Composable
private fun ChildNavigationItem(
    child: NavigationChild,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            Color(0xFFF1F8F4) // Very light green
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bullet point
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            CrudColors.Primary
                        } else {
                            Color(0xFFBBBBBB)
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = child.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) {
                    CrudColors.Primary
                } else {
                    CrudColors.TextPrimary
                },
                modifier = Modifier.weight(1f)
            )
            
            child.badge?.let { badge ->
                BadgeDisplay(badge = badge)
            }
        }
    }
}

@Composable
private fun BadgeDisplay(badge: BadgeInfo) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badge.color,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = badge.text ?: badge.count?.toString() ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showConfirmDialog = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Đăng xuất",
                modifier = Modifier.size(24.dp),
                tint = CrudColors.DangerAction
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Đăng xuất",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = CrudColors.DangerAction
            )
        }
    }
    
    // Confirm dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = Color(0xFFFAFCF8),  // Light green background
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = CrudColors.DangerAction.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = CrudColors.DangerAction,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Xác nhận đăng xuất",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C3A0D)
                )
            },
            text = {
                Text(
                    "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?",
                    color = Color(0xFF4A5A3A)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrudColors.DangerAction,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Đăng xuất",
                        color = Color.White,  // ✅ Text màu trắng
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = CrudColors.Primary  // Xanh olive
                    )
                ) {
                    Text(
                        "Hủy",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@Composable
private fun DrawerFooter() {
    Divider(color = MaterialTheme.colorScheme.outlineVariant)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Inventory System",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Build navigation items dựa trên role của user
 * 
 * Theo tài liệu phân quyền 111 UC:
 * - ADMIN: Toàn quyền, bao gồm cả quản lý người dùng và điều chỉnh tồn kho
 * - WAREHOUSE_STAFF: Quyền hạn chế, chỉ xem/lập phiếu, không duyệt phiếu
 */
private fun buildNavigationItems(currentUser: User?): List<NavigationItem> {
    val userRole = currentUser?.role ?: "GUEST"
    val isAdmin = userRole == "ADMIN"
    val isStaff = userRole == "WAREHOUSE_STAFF"
    
    val allItems = listOf(
        // 1. Dashboard (UC89 - Cả hai role, nhưng staff không thấy giá trị tồn kho)
        NavigationItem(
            id = "dashboard",
            title = "Tổng quan",
            icon = Icons.Default.Dashboard,
            route = "dashboard",
            badge = BadgeInfo(count = 5, color = CrudColors.Primary)
        ),
        
        // 2. Sản phẩm & Danh mục
        // UC08-UC17: Admin - CRUD, Staff - Chỉ xem, tìm kiếm, lọc
        NavigationItem(
            id = "products_categories",
            title = "Sản phẩm & Danh mục",
            icon = Icons.Default.Category,
            children = buildList {
                // UC09, UC10, UC13, UC14: Xem và tìm kiếm - Cả hai role
                add(NavigationChild(
                    id = "products_list",
                    title = "Danh sách sản phẩm",
                    route = "products"
                ))
                
                // UC08: Thêm sản phẩm - CHỈ ADMIN
                if (isAdmin) {
                    add(NavigationChild(
                        id = "products_add",
                        title = "Thêm sản phẩm mới",
                        route = "products/add"
                    ))
                }
                
                // UC18-UC22: Quản lý danh mục - Admin CRUD, Staff chỉ xem
                add(NavigationChild(
                    id = "categories",
                    title = if (isAdmin) "Quản lý danh mục" else "Danh mục sản phẩm",
                    route = "categories"
                ))
            }
        ),
        
        // 3. Nhà cung cấp
        // UC23-UC28: Admin - CRUD, Staff - Chỉ xem
        NavigationItem(
            id = "suppliers",
            title = "Nhà cung cấp",
            icon = Icons.Default.Business,
            route = "suppliers"
        ),
        
        // 4. Quản lý tồn kho
        // UC29-UC38: Xem tồn kho, sổ kho - Cả hai role
        // UC39: Tính giá trị tồn kho - CHỈ ADMIN
        NavigationItem(
            id = "inventory",
            title = "Quản lý tồn kho",
            icon = Icons.Default.Inventory,
            badge = BadgeInfo(text = "NEW", color = CrudColors.OkText),
            children = listOf(
                NavigationChild(
                    id = "stock_overview",
                    title = "Tổng quan tồn kho",
                    route = "inventory/overview",
                    badge = BadgeInfo(count = 3, color = CrudColors.DangerAction)
                ),
                NavigationChild(
                    id = "stock_by_location",
                    title = "Tồn kho theo vị trí",
                    route = "inventory/by-location"
                ),
                NavigationChild(
                    id = "stock_alerts",
                    title = "Cảnh báo tồn kho",
                    route = "inventory/alerts",
                    badge = BadgeInfo(count = 12, color = CrudColors.WarnText)
                ),
                NavigationChild(
                    id = "stock_movements",
                    title = "Lịch sử xuất nhập",
                    route = "inventory/movements"
                )
            )
        ),
        
        // 5. Nhập / Xuất / Kiểm kê
        // UC40-UC67: Lập phiếu - Cả hai role, Duyệt phiếu - CHỈ ADMIN
        NavigationItem(
            id = "operations",
            title = "Phiếu nhập / xuất",
            icon = Icons.Default.SwapHoriz,
            children = buildList {
                // UC40-UC48: Phiếu nhập kho - Cả hai role lập, Admin duyệt
                add(NavigationChild(
                    id = "import_receipt",
                    title = "Phiếu nhập kho",
                    route = "receipts/import"
                ))
                
                // UC49-UC58: Phiếu xuất kho - Cả hai role lập, Admin duyệt
                add(NavigationChild(
                    id = "export_receipt",
                    title = "Phiếu xuất kho",
                    route = "receipts/export"
                ))
                
                // UC59-UC67: Kiểm kê - Cả hai role đếm, Admin xác nhận
                add(NavigationChild(
                    id = "stock_take",
                    title = "Phiếu kiểm kê",
                    route = "stock-take"
                ))
                
                // UC65: Điều chỉnh tồn kho - CHỈ ADMIN
                if (isAdmin) {
                    add(NavigationChild(
                        id = "inventory_adjustment",
                        title = "Điều chỉnh tồn kho",
                        route = "inventory/adjustment"
                    ))
                }
                
                // Lịch sử phiếu - Cả hai role
                add(NavigationChild(
                    id = "receipt_history",
                    title = "Lịch sử phiếu",
                    route = "receipts/history"
                ))
            }
        ),
        
        // 6. Tính năng nâng cao
        // UC68-UC88: Quét mã, lô hàng, chatbot - Cả hai role
        NavigationItem(
            id = "advanced",
            title = "Tính năng nâng cao",
            icon = Icons.Default.AutoAwesome,
            children = listOf(
                // UC68-UC72: Quét mã vạch
                NavigationChild(
                    id = "barcode_scan",
                    title = "Quét mã vạch / QR",
                    route = "barcode/scan"
                ),
                // UC73-UC80: Quản lý lô hàng
                NavigationChild(
                    id = "batch_management",
                    title = "Quản lý lô hàng",
                    route = "batches"
                ),
                // UC81-UC88: Trợ lý AI
                NavigationChild(
                    id = "ai_assistant",
                    title = "Trợ lý AI",
                    route = "assistant"
                )
            )
        ),
        
        // 7. Báo cáo & Thống kê
        // UC89-UC111: Báo cáo - Cả hai role
        // UC91: Báo cáo giá trị tồn kho - CHỈ ADMIN
        NavigationItem(
            id = "reports",
            title = "Báo cáo & Thống kê",
            icon = Icons.Default.Analytics,
            children = buildList {
                // UC98: Báo cáo tồn kho - Cả hai role
                add(NavigationChild(
                    id = "reports_inventory",
                    title = "Báo cáo tồn kho",
                    route = "reports/inventory"
                ))
                
                // UC99-UC100: Báo cáo nhập/xuất - Cả hai role
                add(NavigationChild(
                    id = "reports_movements",
                    title = "Báo cáo xuất nhập",
                    route = "reports/movements"
                ))
                
                // UC102: Báo cáo hàng cận hạn - Cả hai role
                add(NavigationChild(
                    id = "reports_expiry",
                    title = "Báo cáo hàng cận hạn",
                    route = "reports/expiry"
                ))
                
                // UC103: Báo cáo chênh lệch kiểm kê - Cả hai role
                add(NavigationChild(
                    id = "reports_stocktake",
                    title = "Báo cáo kiểm kê",
                    route = "reports/stocktake"
                ))
                
                // UC91: Báo cáo giá trị tồn kho (giá vốn) - CHỈ ADMIN
                if (isAdmin) {
                    add(NavigationChild(
                        id = "reports_value",
                        title = "Báo cáo giá trị tồn kho",
                        route = "reports/value"
                    ))
                }
            }
        )
    )
    
    // 8. Quản lý người dùng - CHỈ ADMIN (UC03, UC04)
    val adminOnlyItems = if (isAdmin) {
        listOf(
            NavigationItem(
                id = "staff_management",
                title = "Quản lý nhân viên",
                icon = Icons.Default.People,
                route = "STAFF_LIST",
                requiredRoles = listOf("ADMIN")
            ),
            NavigationItem(
                id = "settings",
                title = "Quản lý hệ thống",
                icon = Icons.Default.Settings,
                requiredRoles = listOf("ADMIN"),
                children = listOf(
                    // UC33: Thiết lập ngưỡng tồn kho - CHỈ ADMIN
                    NavigationChild(
                        id = "warehouses",
                        title = "Quản lý kho",
                        route = "settings/warehouses"
                    ),
                    NavigationChild(
                        id = "system",
                        title = "Cấu hình hệ thống",
                        route = "settings/system"
                    )
                )
            )
        )
    } else {
        emptyList()
    }
    
    // Filter based on role
    return (allItems + adminOnlyItems).filter { item ->
        item.requiredRoles == null || item.requiredRoles.contains(userRole)
    }
}
