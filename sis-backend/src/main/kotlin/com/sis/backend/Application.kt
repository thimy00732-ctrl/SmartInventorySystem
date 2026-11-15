
package com.sis.backend

import com.sis.backend.config.DatabaseConfig
import com.sis.backend.config.configureStatusPages
import com.sis.backend.controller.CategoryController
import com.sis.backend.controller.ProductController
import com.sis.backend.controller.SupplierController
import com.sis.backend.controller.InventoryController
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.repository.ProductRepository
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.repository.InventoryRepository
import com.sis.backend.repository.WarehouseRepository
import com.sis.backend.service.CategoryService
import com.sis.backend.service.ProductService
import com.sis.backend.service.SupplierService
import com.sis.backend.service.InventoryService
import com.sis.backend.routes.categoryRoutes
import com.sis.backend.routes.productRoutes
import com.sis.backend.routes.supplierRoutes
import com.sis.backend.routes.inventoryRoutes
import com.sis.backend.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.sql.Connection
import javax.sql.DataSource

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
    // Cấu hình xử lý ngoại lệ tập trung
    configureStatusPages()
    
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }

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

        // Authentication & Authorization Routes (UC01-UC07)
        authRoutes(dataSource)

        // Category CRUD Routes
        categoryRoutes(
            CategoryController(
                CategoryService(
                    CategoryRepository(dataSource)
                )
            )
        )

        // Product CRUD Routes
        productRoutes(
            productController = ProductController(
                ProductService(
                    ProductRepository(dataSource),
                    CategoryRepository(dataSource),
                    SupplierRepository(dataSource)
                )
            ),
            categoryRepository = CategoryRepository(dataSource),
            supplierRepository = SupplierRepository(dataSource)
        )

        // Supplier CRUD Routes
        supplierRoutes(
            SupplierController(
                SupplierService(
                    SupplierRepository(dataSource)
                )
            )
        )

        // Inventory Routes
        inventoryRoutes(
            inventoryController = InventoryController(
                InventoryService(
                    InventoryRepository(dataSource)
                )
            ),
            warehouseRepository = WarehouseRepository(dataSource),
            categoryRepository = CategoryRepository(dataSource),
            supplierRepository = SupplierRepository(dataSource)
        )
    }
}

