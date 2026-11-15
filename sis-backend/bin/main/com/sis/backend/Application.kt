
package com.sis.backend

import com.sis.backend.config.DatabaseConfig
import com.sis.backend.config.configureStatusPages
import com.sis.backend.controller.AlertController
import com.sis.backend.controller.CategoryController
import com.sis.backend.controller.InventoryController
import com.sis.backend.controller.ProductController
import com.sis.backend.controller.StockLedgerController
import com.sis.backend.controller.SupplierController
import com.sis.backend.controller.ThresholdController
import com.sis.backend.controller.TransactionController
import com.sis.backend.controller.ValuationController
import com.sis.backend.repository.AlertRepository
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.InventoryRepository
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.StockLedgerRepository
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.repository.ThresholdRepository
import com.sis.backend.repository.TransactionRepository
import com.sis.backend.repository.ValuationRepository
import com.sis.backend.repository.WarehouseRepository
import com.sis.backend.routes.alertRoutes
import com.sis.backend.routes.categoryRoutes
import com.sis.backend.routes.inventoryRoutes
import com.sis.backend.routes.productRoutes
import com.sis.backend.routes.stockLedgerRoutes
import com.sis.backend.routes.supplierRoutes
import com.sis.backend.routes.transactionRoutes
import com.sis.backend.routes.valuationRoutes
import com.sis.backend.service.AlertService
import com.sis.backend.service.CategoryService
import com.sis.backend.service.InventoryService
import com.sis.backend.service.ProductService
import com.sis.backend.service.StockLedgerService
import com.sis.backend.service.SupplierService
import com.sis.backend.service.ThresholdService
import com.sis.backend.service.TransactionService
import com.sis.backend.service.ValuationService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.sql.Connection
import javax.sql.DataSource
import com.sis.backend.security.AuthContract
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

/*UPDATE
*import com.sis.backend.repository.InventoryRepository
*Bổ sung thêm repository InventoryRepository(dataSource)
* */
lateinit var dataSource: DataSource
    private set

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Cài đặt StatusPages trước để xử lý exception
    configureStatusPages()
    
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }
    
    // TODO: Người làm JWT cần cài đặt Authentication provider ở đây
    // Ví dụ:
    // install(Authentication) {
    //     jwt(AuthContract.PROVIDER) {
    //         realm = "Smart Inventory System"
    //         verifier(makeJwtVerifier(...))
    //         validate { credential ->
    //             val userId = credential.payload.getClaim(AuthContract.USER_ID_CLAIM).asLong()
    //             val role = credential.payload.getClaim(AuthContract.ROLE_CLAIM).asString()
    //             
    //             if (userId != null && role != null) {
    //                 JWTPrincipal(credential.payload)
    //             } else {
    //                 null
    //             }
    //         }
    //     }
    // }
    // 
    // QUAN TRỌNG:
    // - Provider name PHẢI là AuthContract.PROVIDER ("jwt")
    // - Claim role PHẢI là AuthContract.ROLE_CLAIM ("role")
    // - Claim userId PHẢI là AuthContract.USER_ID_CLAIM ("userId")
    // - KHÔNG cần sửa RoleAuthorization hay route sau khi cài JWT

    // Khởi tạo kết nối database
    dataSource = DatabaseConfig.createDataSource()

    routing {
        // Kiểm tra backend
        get("/health") {
            call.respond(
                mapOf(
                    "status" to "OK",
                    "service" to "Smart Inventory Backend"
                )
            )
        }

        // Kiểm tra kết nối database
        get("/health/database") {
            try {
                dataSource.connection.use { connection: Connection ->
                    connection.createStatement().use { statement ->
                        statement.executeQuery("SELECT 1").use {
                            call.respond(
                                mapOf(
                                    "status" to "OK",
                                    "database" to "connected"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    io.ktor.http.HttpStatusCode.ServiceUnavailable,
                    mapOf(
                        "status" to "ERROR",
                        "database" to "disconnected",
                        "message" to (e.message ?: "Unknown database error")
                    )
                )
            }
        }

        // CRUD danh mục - Task 9

        categoryRoutes(
            CategoryController(
                CategoryService(
                    CategoryRepository(dataSource)
                )
            )
        )


        // CRUD sản phẩm - Task 10 + UC33 Thresholds
        productRoutes(
            productController = ProductController(
                ProductService(
                    ProductRepository(dataSource),
                    CategoryRepository(dataSource),
                    SupplierRepository(dataSource)
                )
            ),
            thresholdController = ThresholdController(
                ThresholdService(
                    ProductRepository(dataSource),
                    ThresholdRepository(dataSource)
                )
            ),
            categoryRepository = CategoryRepository(dataSource),
            supplierRepository = SupplierRepository(dataSource)
        )

        //CRUD supplier

        supplierRoutes(
            SupplierController(
                SupplierService(
                    SupplierRepository(dataSource)
                )
            )
        )

        // Sprint 2 - Inventory API (UC29-32)
        inventoryRoutes(
            inventoryController = InventoryController(
                InventoryService(
                    InventoryRepository(dataSource),
                    ProductRepository(dataSource)
                )
            ),
            warehouseRepository = WarehouseRepository(dataSource),
            categoryRepository = CategoryRepository(dataSource),
            supplierRepository = SupplierRepository(dataSource)
        )
        
        // Sprint 2 Đợt 2 - Alerts API (UC34, UC35)
        alertRoutes(
            alertController = AlertController(
                AlertService(
                    AlertRepository(dataSource)
                )
            )
        )
        
        // Sprint 2 Đợt 3 - Valuation API (UC39)
        valuationRoutes(
            valuationController = ValuationController(
                ValuationService(
                    ValuationRepository(dataSource)
                )
            )
        )
        
        // Sprint 2 Đợt 4 - Stock Ledger API (UC36)
        stockLedgerRoutes(
            stockLedgerController = StockLedgerController(
                StockLedgerService(
                    StockLedgerRepository(dataSource)
                )
            )
        )
        
        // Sprint 2 Đợt 4 - Transactions API (UC37, UC38)
        transactionRoutes(
            transactionController = TransactionController(
                TransactionService(
                    TransactionRepository(dataSource)
                )
            )
        )
    }
}
