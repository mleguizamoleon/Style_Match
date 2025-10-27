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

/**
 * Clasificador de aprendizaje automático para determinar la forma del rostro a partir de un bitmap.
 * Esta clase ahora es la ÚNICA fuente para la detección de la forma del rostro,
 * reemplazando el antiguo sistema basado en reglas para mayor precisión.
 */
class FaceShapeClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    private val modelFileName = "face_shape_model.tflite" //
    private val labelFileName = "face_shape_labels.txt" //
    private val inputImageWidth = 224 //
    private val inputImageHeight = 224 //
    private val numClasses = 5 //

    // La normalización debe coincidir con la que se usó para entrenar el modelo.
    private val normalizeMean = floatArrayOf(0.485f, 0.456f, 0.406f) //
    private val normalizeStd = floatArrayOf(0.229f, 0.224f, 0.225f) //

    private lateinit var probabilityBuffer: TensorBuffer
    private val TAG = "FaceShapeClassifier"

    init {
        try {
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, modelFileName) //
            val options = Interpreter.Options().apply {
                // Usar un solo hilo a menudo mejora la estabilidad en una amplia gama de dispositivos.
                setNumThreads(1) //
            }
            interpreter = Interpreter(modelBuffer, options) //
            labels = FileUtil.loadLabels(context, labelFileName) //

            if (labels.size != numClasses) { //
                Log.e(TAG, "Error: El número de etiquetas (${labels.size}) no coincide con numClasses ($numClasses).") //
            }

            // Inicializar el buffer de salida una sola vez para reutilizarlo en cada inferencia.
            probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, numClasses), DataType.FLOAT32) //

            Log.i(TAG, "Modelo TFLite de forma de rostro y etiquetas cargados. Clases: ${labels.joinToString()}") //
        } catch (e: IOException) {
            Log.e(TAG, "Error al cargar el modelo de forma de rostro o las etiquetas: ${e.message}", e) //
            interpreter = null
        }
    }

    fun classify(bitmap: Bitmap): FaceAnalyzer.FaceShape {
        if (interpreter == null || labels.isEmpty()) { //
            Log.e(TAG, "El intérprete de forma de rostro o las etiquetas no están inicializados.") //
            return FaceAnalyzer.FaceShape.UNKNOWN //
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR)) //
            .add(NormalizeOp(normalizeMean, normalizeStd)) //
            .build() //

        var tensorImage = TensorImage(DataType.FLOAT32) //
        tensorImage.load(bitmap) //
        tensorImage = imageProcessor.process(tensorImage) //

        try {
            interpreter?.run(tensorImage.buffer, probabilityBuffer.buffer.rewind()) //
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la inferencia de forma de rostro: ${e.message}", e) //
            return FaceAnalyzer.FaceShape.UNKNOWN //
        }

        val probabilities = probabilityBuffer.floatArray //
        if (probabilities.isEmpty()) { //
            Log.e(TAG, "El array de probabilidades está vacío después de la inferencia.") //
            return FaceAnalyzer.FaceShape.UNKNOWN //
        }

        var maxProb = 0f
        var maxIndex = -1
        probabilities.forEachIndexed { index, probability -> //
            if (probability > maxProb) { //
                maxProb = probability //
                maxIndex = index //
            }
        }

        Log.d(TAG, "Probabilidades de forma de rostro: ${probabilities.joinToString { String.format("%.2f", it) }}") //

        if (maxIndex != -1 && maxIndex < labels.size) {
            val predictedLabel = labels[maxIndex] //
            Log.i(TAG, "Forma de rostro predicha: $predictedLabel con confianza $maxProb") //
            return try {
                // Convierte la etiqueta (ej. "Oval") a la constante del enum (ej. OVAL)
                FaceAnalyzer.FaceShape.valueOf(predictedLabel.uppercase().replace(" ", "_")) //
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Etiqueta de forma de rostro desconocida desde el modelo: $predictedLabel", e) //
                FaceAnalyzer.FaceShape.UNKNOWN //
            }
        } else {
            Log.e(TAG, "No se pudo determinar la forma del rostro. MaxIndex: $maxIndex, Label count: ${labels.size}") //
            return FaceAnalyzer.FaceShape.UNKNOWN //
        }
    }

    fun close() {
        interpreter?.close() //
        interpreter = null //
        Log.d(TAG, "Recursos de FaceShapeClassifier liberados.") //
    }
}