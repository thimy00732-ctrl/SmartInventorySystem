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
                .fillMaxSize()
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
        
        Spacer(modifier = Modifier.weight(1f))
        
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
 */
private fun buildNavigationItems(currentUser: User?): List<NavigationItem> {
    val userRole = currentUser?.role ?: "GUEST"
    
    val allItems = listOf(
        // 1. Dashboard (Direct link)
        NavigationItem(
            id = "dashboard",
            title = "Dashboard",
            icon = Icons.Default.Dashboard,
            route = "dashboard",
            badge = BadgeInfo(count = 5, color = CrudColors.Primary) // Xanh olive
        ),
        
        // 2. Sản phẩm & Danh mục (3 items)
        NavigationItem(
            id = "products_categories",
            title = "Sản phẩm & Danh mục",
            icon = Icons.Default.Category,
            children = listOf(
                NavigationChild(
                    id = "products_list",
                    title = "Danh sách sản phẩm",
                    route = "products"
                ),
                NavigationChild(
                    id = "products_add",
                    title = "Thêm sản phẩm mới",
                    route = "products/add"
                ),
                NavigationChild(
                    id = "categories",
                    title = "Quản lý danh mục",
                    route = "categories"
                )
            )
        ),
        
        // 3. Quản lý tồn kho (4 items)
        NavigationItem(
            id = "inventory",
            title = "Quản lý tồn kho",
            icon = Icons.Default.Inventory,
            badge = BadgeInfo(text = "NEW", color = CrudColors.OkText), // Xanh lá
            children = listOf(
                NavigationChild(
                    id = "stock_overview",
                    title = "Tổng quan tồn kho",
                    route = "inventory/overview",
                    badge = BadgeInfo(count = 3, color = CrudColors.DangerAction) // Đỏ warning
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
                    badge = BadgeInfo(count = 12, color = CrudColors.WarnText) // Cam warning
                ),
                NavigationChild(
                    id = "stock_movements",
                    title = "Lịch sử xuất nhập",
                    route = "inventory/movements"
                )
            )
        ),
        
        // 4. Nhập / Xuất / Kiểm kê (4 items)
        NavigationItem(
            id = "operations",
            title = "Nhập / Xuất / Kiểm kê",
            icon = Icons.Default.SwapHoriz,
            children = listOf(
                NavigationChild(
                    id = "import_receipt",
                    title = "Phiếu nhập kho",
                    route = "receipts/import"
                ),
                NavigationChild(
                    id = "export_receipt",
                    title = "Phiếu xuất kho",
                    route = "receipts/export"
                ),
                NavigationChild(
                    id = "stock_take",
                    title = "Phiếu kiểm kê",
                    route = "stock-take"
                ),
                NavigationChild(
                    id = "receipt_history",
                    title = "Lịch sử phiếu",
                    route = "receipts/history"
                )
            )
        ),
        
        // 5. Tính năng nâng cao (3 items)
        NavigationItem(
            id = "advanced",
            title = "Tính năng nâng cao",
            icon = Icons.Default.AutoAwesome,
            children = listOf(
                NavigationChild(
                    id = "barcode_scan",
                    title = "Quét mã vạch",
                    route = "barcode/scan"
                ),
                NavigationChild(
                    id = "batch_operations",
                    title = "Xử lý hàng loạt",
                    route = "batch"
                ),
                NavigationChild(
                    id = "suppliers",
                    title = "Nhà cung cấp",
                    route = "suppliers"
                )
            )
        ),
        
        // 6. Báo cáo & Thống kê (2 items)
        NavigationItem(
            id = "reports",
            title = "Báo cáo & Thống kê",
            icon = Icons.Default.Analytics,
            children = listOf(
                NavigationChild(
                    id = "reports_inventory",
                    title = "Báo cáo tồn kho",
                    route = "reports/inventory"
                ),
                NavigationChild(
                    id = "reports_movements",
                    title = "Báo cáo xuất nhập",
                    route = "reports/movements"
                )
            )
        ),
        
        // 7. Cài đặt hệ thống (5 items) - Admin only
        NavigationItem(
            id = "settings",
            title = "Cài đặt hệ thống",
            icon = Icons.Default.Settings,
            requiredRoles = listOf("ADMIN"),
            children = listOf(
                NavigationChild(
                    id = "users",
                    title = "Quản lý người dùng",
                    route = "settings/users"
                ),
                NavigationChild(
                    id = "warehouses",
                    title = "Quản lý kho",
                    route = "settings/warehouses"
                ),
                NavigationChild(
                    id = "locations",
                    title = "Quản lý vị trí",
                    route = "settings/locations"
                ),
                NavigationChild(
                    id = "permissions",
                    title = "Phân quyền",
                    route = "settings/permissions"
                ),
                NavigationChild(
                    id = "system",
                    title = "Cấu hình hệ thống",
                    route = "settings/system"
                )
            )
        )
    )
    
    // Filter based on role
    return allItems.filter { item ->
        item.requiredRoles == null || item.requiredRoles.contains(userRole)
    }
}
