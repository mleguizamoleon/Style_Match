package com.example.stylematch.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.MappedByteBuffer

class FaceSegmenter(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var faceSkinLabelIndex: Int = -1

    private val modelFileName = "MediaPipe-Selfie-Segmentation.tflite"
    private val labelFileName = "selfie_multiclass_labels.txt"
    private val faceSkinLabel = "face-skin"

    private val inputImageWidth = 256
    private val inputImageHeight = 256
    private val inputNormalizationMean = 0.0f
    private val inputNormalizationStd = 255.0f

    private var numOutputClasses: Int = 0
    private lateinit var outputProbabilityBuffer: TensorBuffer
    private val TAG = "FaceSegmenter"

    init {
        try {
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
            val options = Interpreter.Options()
            options.setUseXNNPACK(false)
            // --- LÍNEA CRÍTICA PARA LA ESTABILIDAD ---
            options.setNumThreads(1)

            interpreter = Interpreter(modelBuffer, options)

            labels = FileUtil.loadLabels(context, labelFileName)
            faceSkinLabelIndex = labels.indexOf(faceSkinLabel)
            numOutputClasses = labels.size

            if (faceSkinLabelIndex == -1) {
                Log.e(TAG, "Label '$faceSkinLabel' not found in $labelFileName.")
            }
            if (numOutputClasses <= 0) {
                Log.e(TAG, "$labelFileName is empty or could not be read.")
            } else {
                val probabilityTensorShape = intArrayOf(1, inputImageHeight, inputImageWidth, numOutputClasses)
                outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityTensorShape, DataType.FLOAT32)
            }
            Log.i(TAG, "Face segmentation model and labels loaded.")
        } catch (e: IOException) {
            Log.e(TAG, "Error loading face segmentation model or labels: ${e.message}", e)
            interpreter = null
        }
    }

    fun segment(bitmap: Bitmap): Bitmap? {
        if (interpreter == null || !::outputProbabilityBuffer.isInitialized) {
            Log.e(TAG, "FaceSegmenter not initialized properly. Cannot segment.")
            return null
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(inputNormalizationMean, inputNormalizationStd))
            .build()
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        try {
            interpreter?.run(tensorImage.buffer, outputProbabilityBuffer.buffer.rewind())
        } catch (e: Exception) {
            Log.e(TAG, "Error during face segmentation inference: ${e.message}", e)
            return null
        }

        val probabilities = outputProbabilityBuffer.floatArray
        val maskBitmap = Bitmap.createBitmap(inputImageWidth, inputImageHeight, Bitmap.Config.ARGB_8888)

        for (y in 0 until inputImageHeight) {
            for (x in 0 until inputImageWidth) {
                val pixelBaseIndex = (y * inputImageWidth + x) * numOutputClasses
                if (pixelBaseIndex + faceSkinLabelIndex < probabilities.size) {
                    val faceSkinProbability = probabilities[pixelBaseIndex + faceSkinLabelIndex]
                    if (faceSkinProbability > 0.5f) {
                        maskBitmap.setPixel(x, y, Color.WHITE)
                    } else {
                        maskBitmap.setPixel(x, y, Color.BLACK)
                    }
                }
            }
        }
        return maskBitmap
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "FaceSegmenter resources released.")
    }
}