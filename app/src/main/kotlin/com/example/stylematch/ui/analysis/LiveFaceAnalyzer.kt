package com.example.stylematch.ui.analysis

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.stylematch.util.FaceDetectionUtil // <-- IMPORTACIÓN AÑADIDA
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class LiveFaceAnalyzer(
    private val faceOverlayView: FaceOverlayView,
    private val onAnalysisResult: (List<Face>, Bitmap?) -> Unit
) : androidx.camera.core.ImageAnalysis.Analyzer {

    private val faceDetector: FaceDetector = FaceDetection.getClient(FaceDetectionUtil.getRealTimeDetectorOptions())

    private val scope = CoroutineScope(Dispatchers.Default)
    var isLivePreviewFrontCamera: Boolean = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val imageWidth = inputImage.width
            val imageHeight = inputImage.height

            scope.launch {
                var bitmap: Bitmap? = null
                try {
                    bitmap = imageProxyToBitmap(imageProxy)
                    val faces = faceDetector.process(inputImage).await()

                    withContext(Dispatchers.Main) {
                        faceOverlayView.updateFaces(
                            faces,
                            imageWidth,
                            imageHeight,
                            faceOverlayView.width,
                            faceOverlayView.height,
                            isLivePreviewFrontCamera
                        )
                    }

                    onAnalysisResult(faces, bitmap)
                } catch (e: Exception) {
                    Log.e("LiveFaceAnalyzer", "La detección de rostros o la conversión a bitmap falló: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        onAnalysisResult(emptyList(), null)
                    }
                } finally {
                    imageProxy.close()
                }
            }
        } else {
            imageProxy.close()
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 90, out)
        val imageBytes = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        return bitmap
    }
}