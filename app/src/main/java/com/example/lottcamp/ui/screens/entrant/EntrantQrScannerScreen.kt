package com.example.lottcamp.ui.screens.entrant

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.lottcamp.ui.components.AppDialog
import com.example.lottcamp.ui.components.AppTopBar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

object EntrantQrScannerTestTags {
    const val BACK_BUTTON = "qr_scanner_back_button"
    const val CAMERA_PREVIEW = "qr_scanner_camera_preview"
    const val INSTRUCTION_TEXT = "qr_scanner_instruction_text"
}

/**
 * Full-screen live QR code scanner powered by CameraX and Google ML Kit.
 *
 * Invokes [onQrScanned] once with the raw QR code payload and shows [AppDialog]
 * if [showNotFoundDialog] is true.
 */
@Composable
fun EntrantQrScannerScreen(
    onQrScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    showNotFoundDialog: Boolean = false,
    onDismissNotFound: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { androidx.camera.view.PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }
    var hasEmittedResult by remember { mutableStateOf(false) }

    LaunchedEffect(showNotFoundDialog) {
        if (!showNotFoundDialog) {
            hasEmittedResult = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        val listener = Runnable {
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { imageAnalysis ->
                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processFrame(
                                imageProxy = imageProxy,
                                scanner = scanner,
                                onDecoded = { rawValue ->
                                    if (!hasEmittedResult && !showNotFoundDialog) {
                                        hasEmittedResult = true
                                        ContextCompat.getMainExecutor(context).execute {
                                            onQrScanned(rawValue)
                                        }
                                    }
                                },
                            )
                        }
                    }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            } catch (e: Exception) {
                Log.e("EntrantQrScannerScreen", "Failed to start camera QR scanner", e)
            }
        }

        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
            scanner.close()
        }
    }

    if (showNotFoundDialog) {
        AppDialog(
            title = "No campsite found",
            message = "No campsite was found for the scanned QR code.",
            dismissText = "Close",
            onDismiss = onClose,
            confirmText = "Scan again",
            onConfirm = onDismissNotFound,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(
            title = "Scan campsite QR",
            onBack = onClose,
            backButtonModifier = Modifier.testTag(EntrantQrScannerTestTags.BACK_BUTTON),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(EntrantQrScannerTestTags.CAMERA_PREVIEW),
            )

            Text(
                text = "Point your camera at a campsite QR code",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag(EntrantQrScannerTestTags.INSTRUCTION_TEXT),
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processFrame(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onDecoded: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val rawValue = barcodes.firstNotNullOfOrNull { it.rawValue?.takeIf(String::isNotBlank) }
            if (rawValue != null) {
                onDecoded(rawValue)
            }
        }
        .addOnFailureListener { e ->
            Log.e("EntrantQrScannerScreen", "Barcode scan failed", e)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}