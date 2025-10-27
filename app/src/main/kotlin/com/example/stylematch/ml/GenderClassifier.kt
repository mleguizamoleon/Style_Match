package com.example.stylematch.ml

import android.content.Context
import android.graphics.Bitmap
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
import kotlin.math.max

class GenderClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelFileName = "model_gender_q.tflite" // Your quantized gender model
    private val inputImageWidth = 128
    private val inputImageHeight = 128
    private lateinit var probabilityBuffer: TensorBuffer

    //Confidence threshold to avoid uncertain classifications.
    private val CONFIDENCE_THRESHOLD = 0.70f // 70% confidence required

    init {
        try {
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
            val options = Interpreter.Options().apply {
                // Using 1 thread for stability, which is a good practice on diverse devices.
                setNumThreads(1)
            }
            interpreter = Interpreter(modelBuffer, options)
            // The model output is for 2 classes: Male and Female.
            probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 2), DataType.FLOAT32)
            Log.i("GenderClassifier", "Gender TFLite model loaded successfully.")
        } catch (e: IOException) {
            Log.e("GenderClassifier", "Error loading gender model: ${e.message}", e)
            interpreter = null
        }
    }

    fun classify(bitmap: Bitmap): FaceAnalyzer.Gender {
        if (interpreter == null) {
            Log.e("GenderClassifier", "Gender interpreter is not initialized.")
            return FaceAnalyzer.Gender.UNKNOWN
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            // Normalizes the image to [-1, 1], which is expected by this model.
            .add(NormalizeOp(127.5f, 127.5f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        try {
            interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer.rewind())
        } catch (e: Exception) {
            Log.e("GenderClassifier", "Error during gender inference: ${e.message}", e)
            return FaceAnalyzer.Gender.UNKNOWN
        }

        val probabilities = probabilityBuffer.floatArray

        val maleProbability = probabilities[0]
        val femaleProbability = probabilities[1]

        Log.d("GenderClassifier", "Probabilities - Male: %.2f, Female: %.2f".format(maleProbability, femaleProbability))

        val maxProbability = max(maleProbability, femaleProbability)

        // Only return a confident prediction.
        if (maxProbability < CONFIDENCE_THRESHOLD) {
            Log.w("GenderClassifier", "Prediction confidence ($maxProbability) is below threshold ($CONFIDENCE_THRESHOLD). Returning UNKNOWN.")
            return FaceAnalyzer.Gender.UNKNOWN
        }

        return if (maleProbability > femaleProbability) {
            FaceAnalyzer.Gender.MALE
        } else {
            FaceAnalyzer.Gender.FEMALE
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}