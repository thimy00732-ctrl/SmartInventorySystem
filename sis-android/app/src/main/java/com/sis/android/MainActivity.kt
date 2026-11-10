package com.sis.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sis.android.data.model.InventoryItemResponse
import com.sis.android.di.AppModule
import com.sis.android.ui.inventory.UpdateReservationDialog

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                DirectReservationDialogScreen()
            }
        }
    }

    @Composable
    private fun DirectReservationDialogScreen() {
        val inventoryRepository = remember { AppModule.provideInventoryRepository() }

        var inventoryItem by remember { mutableStateOf<InventoryItemResponse?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showDialog by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            val result = inventoryRepository.getInventoryList()
            result.onSuccess { list ->
                inventoryItem = list.firstOrNull { it.inventoryId == 1 } ?: list.firstOrNull()
                isLoading = false
            }.onFailure { error ->
                errorMessage = error.message ?: "Không thể kết nối máy chủ"
                isLoading = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F5FA)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = Color(0xFF365314))
                }

                errorMessage != null -> {
                    Text(text = errorMessage!!, color = Color.Red)
                }

                showDialog && inventoryItem != null -> {
                    val item = inventoryItem!!
                    UpdateReservationDialog(
                        inventoryId = item.inventoryId,
                        stockQuantity = item.stockQuantity,
                        reservedQuantity = item.reservedQuantity,
                        repository = inventoryRepository,
                        onDismiss = {
                            showDialog = false
                        },
                        onSuccess = { newQuantity ->
                            showDialog = false
                        }
                    )
                }
            }
        }
    }
}
