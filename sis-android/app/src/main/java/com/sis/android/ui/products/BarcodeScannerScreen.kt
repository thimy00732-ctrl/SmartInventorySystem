
package com.sis.android.ui.products

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.sis.android.ui.theme.color.CrudColors
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Kiểm tra quyền Camera
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Trạng thái quét mã
    var isBarcodeScanned by remember {
        mutableStateOf(false)
    }

    // Ngăn việc xử lý cùng một lúc nhiều mã vạch
    val scanHandled = remember {
        AtomicBoolean(false)
    }

    // Quản lý Camera và ML Kit
    val barcodeScanner = remember {
        BarcodeScanning.getClient()
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val mainExecutor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    val cameraProviderState = remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    val isDisposed = remember {
        AtomicBoolean(false)
    }

    // Yêu cầu quyền Camera
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Giải phóng tài nguyên khi rời màn hình
    DisposableEffect(Unit) {
        onDispose {
            isDisposed.set(true)

            cameraProviderState.value?.unbindAll()

            barcodeScanner.close()

            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Quét Barcode Sản Phẩm")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CrudColors.Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {

            if (hasCameraPermission) {

                AndroidView(
                    factory = { ctx ->

                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture =
                            ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({

                            if (isDisposed.get()) {
                                return@addListener
                            }

                            try {
                                val cameraProvider =
                                    cameraProviderFuture.get()

                                cameraProviderState.value = cameraProvider

                                val preview = Preview.Builder()
                                    .build()
                                    .also {
                                        it.setSurfaceProvider(
                                            previewView.surfaceProvider
                                        )
                                    }

                                // Cấu hình ML Kit Barcode Scanner
                                val imageAnalysis =
                                    ImageAnalysis.Builder()
                                        .setBackpressureStrategy(
                                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                        )
                                        .build()

                                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

                                    if (scanHandled.get() || isDisposed.get()) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val mediaImage =
                                        getMediaImage(imageProxy)

                                    if (mediaImage == null) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    barcodeScanner.process(image)
                                        .addOnSuccessListener(mainExecutor) { barcodes ->

                                            if (!scanHandled.get()) {

                                                val scannedCode =
                                                    barcodes.firstNotNullOfOrNull {
                                                        it.rawValue
                                                    }

                                                if (scannedCode != null &&
                                                    scanHandled.compareAndSet(
                                                        false,
                                                        true
                                                    )
                                                ) {
                                                    isBarcodeScanned = true

                                                    // Gửi mã Barcode về ViewModel
                                                    onBarcodeScanned(scannedCode)
                                                }
                                            }
                                        }
                                        .addOnFailureListener(mainExecutor) { exception ->
                                            Log.e(
                                                "BarcodeScanner",
                                                "Lỗi quét Barcode",
                                                exception
                                            )
                                        }
                                        .addOnCompleteListener(mainExecutor) {
                                            imageProxy.close()
                                        }
                                }

                                // Khởi động Camera
                                cameraProvider.unbindAll()

                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )

                            } catch (e: Exception) {
                                Log.e(
                                    "BarcodeScanner",
                                    "Không thể khởi động Camera",
                                    e
                                )
                            }

                        }, mainExecutor)

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Hướng dẫn quét Barcode
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (isBarcodeScanned) {
                            "Đã quét mã vạch thành công!"
                        } else {
                            "Đưa mã vạch vào giữa khung hình để quét..."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            } else {

                // Giao diện khi chưa được cấp quyền Camera
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Ứng dụng cần quyền sử dụng Camera để quét mã vạch.",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Cấp quyền Camera")
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(
    androidx.camera.core.ExperimentalGetImage::class
)
private fun getMediaImage(imageProxy: ImageProxy) =
    imageProxy.image