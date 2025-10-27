package com.example.stylematch.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Analiza un rostro para extraer características clave.
 * MEJORA: Ahora utiliza un FaceShapeClassifier inyectado para una detección de forma de rostro
 * precisa basada en ML, en lugar de un sistema de reglas frágil.
 */
class FaceAnalyzer(
    private val context: Context,
    private val faceShapeClassifier: FaceShapeClassifier // <-- MEJORA: Inyectado para precisión
) {
    // Los clasificadores internos se siguen inicializando de forma perezosa (lazy)
    private lateinit var faceSegmenter: FaceSegmenter
    private lateinit var genderClassifier: GenderClassifier

    // Métodos auxiliares para asegurar que los clasificadores internos se carguen solo cuando se necesiten
    private fun getFaceSegmenter(): FaceSegmenter {
        if (!::faceSegmenter.isInitialized) {
            faceSegmenter = FaceSegmenter(context)
        }
        return faceSegmenter
    }

    private fun getGenderClassifier(): GenderClassifier {
        if (!::genderClassifier.isInitialized) {
            genderClassifier = GenderClassifier(context)
        }
        return genderClassifier
    }

    // Estructuras de datos para los resultados del análisis
    data class FaceAnalysis(
        val faceShape: FaceShape,
        val symmetryScore: Float,
        val proportions: FaceProportions,
        val features: List<FaceFeature>,
        val gender: Gender,
        val segmentationWarning: String? = null
    )

    data class FaceProportions(
        val faceLength: Float,
        val cheekboneWidth: Float,
        val foreheadWidth: Float,
        val jawWidth: Float,
        val upperThirdHeight: Float,
        val middleThirdHeight: Float,
        val lowerThirdHeight: Float,
        val jawShape: JawShape
    )

    enum class Gender { MALE, FEMALE, UNKNOWN }
    enum class FaceShape { OVAL, ROUND, SQUARE, HEART, DIAMOND, RECTANGLE, TRIANGLE, UNKNOWN }
    enum class JawShape { ROUNDED, ANGULAR, UNKNOWN }
    enum class FaceFeature {
        HIGH_FOREHEAD, WIDE_JAW, PROMINENT_CHEEKBONES, NARROW_CHIN, BALANCED_FEATURES,
        LONG_UPPER_THIRD, LONG_MIDDLE_THIRD, LONG_LOWER_THIRD,
        SHORT_UPPER_THIRD, SHORT_MIDDLE_THIRD, SHORT_LOWER_THIRD
    }

    /**
     * Función principal de análisis. Orquesta la segmentación, clasificación de género,
     * y la detección de forma de rostro y características.
     */
    fun analyzeFace(face: Face, originalBitmap: Bitmap): FaceAnalysis {
        val faceContourMLKit = face.getContour(FaceContour.FACE)?.points
            ?: return createDefaultAnalysis(FaceShape.UNKNOWN, "No se detectó el contorno facial.")

        var segmentationWarningMessage: String? = null
        val skinMaskBitmap = try {
            getFaceSegmenter().segment(originalBitmap)
        } catch (e: Exception) {
            Log.e("FaceAnalyzer", "La segmentación facial falló", e)
            null
        }

        if (skinMaskBitmap == null) {
            segmentationWarningMessage = "Precisión del análisis reducida: Falló la segmentación de piel."
        }

        val faceBounds = face.boundingBox
        val croppedFaceBitmap = try {
            Bitmap.createBitmap(
                originalBitmap,
                faceBounds.left.coerceAtLeast(0),
                faceBounds.top.coerceAtLeast(0),
                faceBounds.width().coerceAtMost(originalBitmap.width - faceBounds.left),
                faceBounds.height().coerceAtMost(originalBitmap.height - faceBounds.top)
            )
        } catch (e: IllegalArgumentException) {
            Log.e("FaceAnalyzer", "Falló al recortar el bitmap del rostro", e)
            null
        }

        // --- MEJORA CRÍTICA: Usar el clasificador de ML en lugar de reglas ---
        val detectedFaceShape = croppedFaceBitmap?.let {
            faceShapeClassifier.classify(it)
        } ?: FaceShape.UNKNOWN

        val detectedGender = croppedFaceBitmap?.let { getGenderClassifier().classify(it) } ?: Gender.UNKNOWN

        val proportions = calculateProportions(face, faceContourMLKit, skinMaskBitmap)
        val symmetryScore = calculateSymmetry(face)
        val features = identifyFeatures(proportions, detectedFaceShape)

        return FaceAnalysis(
            faceShape = detectedFaceShape, //
            symmetryScore = symmetryScore,
            proportions = proportions,
            features = features,
            gender = detectedGender,
            segmentationWarning = segmentationWarningMessage
        )
    }

    // El resto de la lógica para calcular proporciones y características se mantiene,
    // ya que sigue siendo información valiosa para el motor de recomendación.

    private fun getContourPoints(face: Face, contourType: Int): List<PointF>? {
        return face.getContour(contourType)?.points
    }

    private fun getLandmarkPoint(face: Face, landmarkType: Int): PointF? {
        return face.getLandmark(landmarkType)?.position
    }

    private fun calculateProportions(
        face: Face,
        faceOvalContour: List<PointF>,
        skinMaskBitmap: Bitmap?
    ): FaceProportions {
        var minXFace = Float.MAX_VALUE
        var maxXFace = Float.MIN_VALUE
        var minYFace = Float.MAX_VALUE
        var maxYFace = Float.MIN_VALUE
        faceOvalContour.forEach { point ->
            minXFace = min(minXFace, point.x)
            maxXFace = max(maxXFace, point.x)
            minYFace = min(minYFace, point.y)
            maxYFace = max(maxYFace, point.y)
        }

        val cheekboneWidth = maxXFace - minXFace
        var trichionY = minYFace
        var faceLength = maxYFace - minYFace

        skinMaskBitmap?.let {
            findSkinBounds(it)?.let { skinBounds ->
                val faceBoundingBox = face.boundingBox
                if (it.height > 0 && faceBoundingBox.height() > 0) {
                    val scaledMaskTop = (skinBounds.top.toFloat() / it.height.toFloat()) * faceBoundingBox.height()
                    trichionY = min(minYFace, faceBoundingBox.top + scaledMaskTop)
                    faceLength = maxYFace - trichionY
                }
            }
        }

        val mentonY = maxYFace
        val foreheadPoints = faceOvalContour.filter { it.y >= trichionY && it.y <= trichionY + (faceLength / 3.5f) }
        val foreheadWidth = if (foreheadPoints.isNotEmpty()) {
            foreheadPoints.maxOf { it.x } - foreheadPoints.minOf { it.x }
        } else {
            cheekboneWidth * 0.88f
        }

        val jawPoints = faceOvalContour.filter { it.y >= mentonY - (faceLength / 3.0f) }
        val jawWidth = if (jawPoints.size > 2) {
            jawPoints.maxOf { it.x } - jawPoints.minOf { it.x }
        } else {
            cheekboneWidth * 0.78f
        }
        val jawShape = if (jawWidth > cheekboneWidth * 0.85f) JawShape.ANGULAR else JawShape.ROUNDED

        val glabellaY = getContourPoints(face, FaceContour.NOSE_BRIDGE)?.minOfOrNull { it.y }
            ?: (trichionY + (faceLength / 3.0f))
        val subnasaleY = getLandmarkPoint(face, FaceLandmark.NOSE_BASE)?.y ?: (glabellaY + (faceLength / 3.0f))

        val upperThirdHeight = abs(glabellaY - trichionY)
        val middleThirdHeight = abs(subnasaleY - glabellaY)
        val lowerThirdHeight = abs(mentonY - subnasaleY)

        return FaceProportions(
            faceLength = faceLength,
            cheekboneWidth = cheekboneWidth,
            foreheadWidth = foreheadWidth,
            jawWidth = jawWidth,
            upperThirdHeight = upperThirdHeight,
            middleThirdHeight = middleThirdHeight,
            lowerThirdHeight = lowerThirdHeight,
            jawShape = jawShape
        )
    }

    // --- MEJORA: La función `determineFaceShape` ha sido eliminada ---

    private fun findSkinBounds(skinMaskBitmap: Bitmap): Rect? {
        val width = skinMaskBitmap.width
        val height = skinMaskBitmap.height
        var minX = width; var maxX = -1; var minY = height; var maxY = -1
        var found = false
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (skinMaskBitmap.getPixel(x, y) == Color.WHITE) {
                    found = true
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        return if (found) Rect(minX, minY, maxX, maxY) else null
    }

    private fun calculateSymmetry(face: Face): Float {
        val leftEye = getLandmarkPoint(face, FaceLandmark.LEFT_EYE)
        val rightEye = getLandmarkPoint(face, FaceLandmark.RIGHT_EYE)
        val nose = getLandmarkPoint(face, FaceLandmark.NOSE_BASE)
        val mouth = getLandmarkPoint(face, FaceLandmark.MOUTH_BOTTOM) ?: getLandmarkPoint(face, FaceLandmark.MOUTH_LEFT)

        if (leftEye == null || rightEye == null || nose == null || mouth == null) return 0.5f

        val midEyesX = (leftEye.x + rightEye.x) / 2
        val eyeSpan = abs(rightEye.x - leftEye.x)
        if (eyeSpan == 0f) return 0.5f

        val totalDev = (abs(nose.x - midEyesX) + abs(mouth.x - midEyesX)) / 2
        return (1.0f - (totalDev / (eyeSpan / 2.0f))).coerceIn(0f, 1f)
    }

    private fun identifyFeatures(p: FaceProportions, shape: FaceShape): List<FaceFeature> {
        val features = mutableListOf<FaceFeature>()
        if (p.faceLength <= 0) return emptyList()

        val third = p.faceLength / 3.0f
        if (p.upperThirdHeight > third * 1.15f) features.add(FaceFeature.LONG_UPPER_THIRD)
        else if (p.upperThirdHeight < third * 0.85f) features.add(FaceFeature.SHORT_UPPER_THIRD)

        if (p.middleThirdHeight > third * 1.15f) features.add(FaceFeature.LONG_MIDDLE_THIRD)
        else if (p.middleThirdHeight < third * 0.85f) features.add(FaceFeature.SHORT_MIDDLE_THIRD)

        if (p.lowerThirdHeight > third * 1.15f) features.add(FaceFeature.LONG_LOWER_THIRD)
        else if (p.lowerThirdHeight < third * 0.85f) features.add(FaceFeature.SHORT_LOWER_THIRD)

        if (p.jawWidth > p.cheekboneWidth * 0.90f) features.add(FaceFeature.WIDE_JAW)
        if (p.cheekboneWidth > p.foreheadWidth * 1.1f && p.cheekboneWidth > p.jawWidth * 1.1f) features.add(FaceFeature.PROMINENT_CHEEKBONES)
        if (p.jawWidth < p.foreheadWidth * 0.85f) features.add(FaceFeature.NARROW_CHIN)
        if (features.isEmpty() && shape == FaceShape.OVAL) features.add(FaceFeature.BALANCED_FEATURES)

        return features.distinct()
    }

    private fun createDefaultAnalysis(shape: FaceShape, error: String) = FaceAnalysis(
        faceShape = shape,
        symmetryScore = 0.0f,
        gender = Gender.UNKNOWN,
        proportions = FaceProportions(0f, 0f, 0f, 0f, 0f, 0f, 0f, JawShape.UNKNOWN),
        features = emptyList(),
        segmentationWarning = if (shape == FaceShape.UNKNOWN) error else null
    )

    fun close() {
        // Cierra los clasificadores que esta clase gestiona internamente.
        if (::faceSegmenter.isInitialized) faceSegmenter.close()
        if (::genderClassifier.isInitialized) genderClassifier.close()
        // No cerramos faceShapeClassifier aquí porque su ciclo de vida es gestionado por Hilt.
        Log.d("FaceAnalyzer", "Recursos internos de FaceAnalyzer liberados.")
    }
}