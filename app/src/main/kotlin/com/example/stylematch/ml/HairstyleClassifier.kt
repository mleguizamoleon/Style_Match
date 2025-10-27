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

class HairstyleClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val modelFileName = "hairstyle_model_final_directKeras_valAcc05203.tflite" //
    private val labelFileName = "hairstyle_labels.txt"
    private val inputImageWidth = 224
    private val inputImageHeight = 224
    private val numClasses = 61

    // Crear el buffer de salida UNA SOLA VEZ y reutilizarlo
    private lateinit var probabilityBuffer: TensorBuffer

    init {
        try {
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName)
            val options = Interpreter.Options()

            // Forzar la ejecución en un solo hilo para máxima estabilidad
            options.setNumThreads(1)

            interpreter = Interpreter(modelBuffer, options)
            labels = FileUtil.loadLabels(context, labelFileName)

            // Se inicializa el buffer aquí
            probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, numClasses), DataType.FLOAT32)

            Log.i("HairstyleClassifier", "Modelo TFLite y etiquetas cargados correctamente.")
        } catch (e: IOException) {
            Log.e("HairstyleClassifier", "Error al cargar el modelo o las etiquetas: ${e.message}", e)
        }
    }

    fun classify(bitmap: Bitmap): Map<String, Float> {
        if (interpreter == null || labels.isEmpty()) { //
            Log.e("HairstyleClassifier", "El intérprete o las etiquetas no están inicializados.")
            return emptyMap()
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            // Normaliza los valores de píxeles (0-255) al rango [0.0, 1.0].
            .add(NormalizeOp(0.0f, 255.0f)) //
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        try {
            // Se reutiliza el buffer existente en cada inferencia
            interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer.rewind())
        } catch (e: Exception) {
            Log.e("HairstyleClassifier", "Error durante la inferencia: ${e.message}", e)
            return emptyMap()
        }

        val probabilities = probabilityBuffer.floatArray
        val resultMap = mutableMapOf<String, Float>()
        probabilities.forEachIndexed { index, probability ->
            if (index < labels.size) {
                resultMap[labels[index]] = probability
            }
        }
        return resultMap //
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}