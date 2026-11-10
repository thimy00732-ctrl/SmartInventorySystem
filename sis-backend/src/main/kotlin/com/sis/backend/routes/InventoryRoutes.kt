package com.sis.backend.routes

import com.sis.backend.controller.InventoryController
import com.sis.backend.dto.UpdateReservationRequest
import com.sis.backend.repository.InventoryRepository
import com.sis.backend.service.InventoryService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import javax.sql.DataSource

fun Route.inventoryRoutes(dataSource: DataSource) {

    val repository = InventoryRepository(dataSource)
    val service = InventoryService(repository)
    val controller = InventoryController(service)

    route("/api/inventory") {

        get {
            try {
                val list = repository.findAll()
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Lỗi máy chủ khi lấy danh sách tồn kho")
                )
            }
        }

        patch("/{inventoryId}/reservation") {
            try {
                val inventoryId = call.parameters["inventoryId"]?.toIntOrNull()

                if (inventoryId == null || inventoryId <= 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("message" to "Mã tồn kho không hợp lệ")
                    )
                    return@patch
                }

                val request = call.receive<UpdateReservationRequest>()

                val result = controller.updateReservation(
                    inventoryId,
                    request
                )

                call.respond(HttpStatusCode.OK, result)

            } catch (e: NoSuchElementException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to (e.message ?: "Không tìm thấy tồn kho"))
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to (e.message ?: "Dữ liệu không hợp lệ"))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Lỗi máy chủ khi cập nhật giữ chỗ")
                )
            }
        }
    }
}
