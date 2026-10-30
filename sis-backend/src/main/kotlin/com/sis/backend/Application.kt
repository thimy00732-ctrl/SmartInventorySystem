
package com.sis.backend

import com.sis.backend.config.DatabaseConfig
import com.sis.backend.controller.CategoryController
import com.sis.backend.repository.CategoryRepository
import com.sis.backend.routes.categoryRoutes
import com.sis.backend.service.CategoryService
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
import com.sis.backend.controller.ProductController
import com.sis.backend.repository.ProductRepository
import com.sis.backend.routes.productRoutes
import com.sis.backend.service.ProductService
import com.sis.backend.repository.SupplierRepository
import com.sis.backend.service.SupplierService
import com.sis.backend.controller.SupplierController
import com.sis.backend.routes.supplierRoutes
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

        // CRUD danh mục - Task 9

        categoryRoutes(
            CategoryController(
                CategoryService(
                    CategoryRepository(dataSource)
                )
            )
        )


        // CRUD sản phẩm - Task 10
        productRoutes(
            ProductController(
                ProductService(
                    ProductRepository(dataSource)
                )
            )
        )

        //CRUD supplier

        supplierRoutes(
            SupplierController(
                SupplierService(
                    SupplierRepository(dataSource)
                )
            )
        )
    }
}
