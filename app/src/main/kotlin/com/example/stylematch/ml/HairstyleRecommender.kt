package com.example.stylematch.ml

import com.example.stylematch.ml.FaceAnalyzer.FaceAnalysis
import com.example.stylematch.ml.FaceAnalyzer.FaceFeature
import com.example.stylematch.ml.FaceAnalyzer.FaceShape
import com.example.stylematch.ml.FaceAnalyzer.Gender

/**
 * MEJORA: El motor de recomendaciones ha sido refactorizado para usar un sistema de puntuación
 * modular y basado en reglas, lo que lo hace más limpio, legible y fácil de extender
 * en comparación con el anterior bloque `when` anidado.
 */
class HairstyleRecommender {

    data class HairstyleRecommendation(
        val hairstyleName: String,
        val attributes: HairstyleAttributes?,
        val confidence: Float,
        val reasons: List<String>,
        var imageUrl: String? = null
    )

    // Define una condición de puntuación para una característica o forma de rostro.
    private data class ScoreCondition(
        val score: Float,
        val reason: String,
        // Una función lambda que evalúa si los atributos de un peinado cumplen la condición.
        val check: (HairstyleAttributes) -> Boolean
    )

    // Agrupa las reglas por forma de rostro para una fácil consulta.
    private val faceShapeRules: Map<FaceShape, List<ScoreCondition>> = mapOf(
        FaceShape.OVAL to listOf(
            ScoreCondition(0.5f, "Tu rostro ovalado es muy versátil y se adapta bien a casi cualquier estilo.", { true }),
            ScoreCondition(-0.25f, "Aun así, estilos con líneas muy drásticas pueden alterar la armonía natural de tu rostro.", { it.structure == HairStructure.MOHAWK || (it.bangs == HairBangs.FULL_BANGS && it.length <= HairLength.CHIN_LENGTH) })
        ),
        FaceShape.ROUND to listOf(
            ScoreCondition(0.6f, "Estilos largos con volumen superior o capas alargan visualmente un rostro redondo.", { it.length >= HairLength.SHOULDER_LENGTH && (it.volume == HairVolume.HIGH || it.layers >= HairLayers.MEDIUM) }),
            ScoreCondition(-0.4f, "Los bobs a la altura del mentón sin volumen superior pueden acentuar la redondez.", { it.length == HairLength.CHIN_LENGTH && it.structure == HairStructure.BOB && it.volume != HairVolume.HIGH })
        ),
        FaceShape.SQUARE to listOf(
            ScoreCondition(0.6f, "La textura ondulada o rizada y las capas suavizan los ángulos marcados de una mandíbula cuadrada.", { it.texture >= HairTexture.WAVY && it.layers >= HairLayers.MEDIUM && it.length >= HairLength.CHIN_LENGTH }),
            ScoreCondition(-0.5f, "Los bobs muy rectos a la altura de la mandíbula pueden endurecer las facciones cuadradas.", { it.structure == HairStructure.BOB && it.length == HairLength.CHIN_LENGTH && it.texture == HairTexture.STRAIGHT })
        ),
        FaceShape.HEART to listOf(
            ScoreCondition(0.6f, "El volumen en la parte inferior y un flequillo suave equilibran una frente ancha.", { (it.bangs == HairBangs.SIDE_SWEPT || it.bangs == HairBangs.CURTAIN_BANGS) || (it.length >= HairLength.CHIN_LENGTH && it.volume > HairVolume.LOW) }),
            ScoreCondition(-0.4f, "Estilos cortos con mucho volumen superior pueden desequilibrar un rostro corazón.", { it.volume == HairVolume.HIGH && it.length < HairLength.CHIN_LENGTH && it.bangs == HairBangs.NONE })
        ),
        FaceShape.RECTANGLE to listOf(
            ScoreCondition(0.6f, "Los flequillos y la longitud media añaden anchura y acortan un rostro alargado.", { it.length >= HairLength.CHIN_LENGTH && it.length <= HairLength.SHOULDER_LENGTH && it.bangs != HairBangs.NONE }),
            ScoreCondition(-0.5f, "Los estilos muy largos y lisos pueden acentuar la longitud del rostro.", { it.length >= HairLength.MID_BACK_LENGTH && it.texture == HairTexture.STRAIGHT && it.volume == HairVolume.LOW })
        ),
        FaceShape.DIAMOND to listOf(
            ScoreCondition(0.65f, "Este estilo añade volumen en la barbilla o suaviza la frente, equilibrando los pómulos anchos.", { it.length == HairLength.CHIN_LENGTH || (it.length == HairLength.SHOULDER_LENGTH && it.layers != HairLayers.NONE) || (it.bangs != HairBangs.NONE && it.bangs != HairBangs.MICRO_BANGS) }),
            ScoreCondition(-0.5f, "El volumen a la altura de las orejas puede ensanchar excesivamente los pómulos.", { it.length == HairLength.EAR_LENGTH && it.volume == HairVolume.HIGH })
        ),
        FaceShape.TRIANGLE to listOf(
            ScoreCondition(0.55f, "El volumen en la parte superior y un flequillo ayudan a equilibrar una mandíbula ancha.", { it.volume >= HairVolume.MEDIUM && it.layers >= HairLayers.MEDIUM && it.length <= HairLength.SHOULDER_LENGTH && it.bangs != HairBangs.NONE }),
            ScoreCondition(-0.45f, "Un estilo con mucho volumen en la mandíbula puede ensancharla aún más.", { it.length == HairLength.CHIN_LENGTH && it.volume == HairVolume.HIGH && it.layers == HairLayers.NONE })
        )
    )

    // Reglas separadas para características faciales específicas.
    private val featureRules: List<Pair<(FaceAnalysis) -> Boolean, ScoreCondition>> = listOf(
        // --- CORRECCIÓN: Se añade el tipo explícito `FaceAnalysis` al parámetro `analysis` ---
        { analysis: FaceAnalysis -> analysis.features.contains(FaceFeature.HIGH_FOREHEAD) || analysis.features.contains(FaceFeature.LONG_UPPER_THIRD) } to
                ScoreCondition(0.30f, "El flequillo ayuda a equilibrar una frente prominente.", { it.bangs != HairBangs.NONE && it.bangs != HairBangs.MICRO_BANGS }),

        // --- CORRECCIÓN: Se añade el tipo explícito `FaceAnalysis` al parámetro `analysis` ---
        { analysis: FaceAnalysis -> analysis.features.contains(FaceFeature.WIDE_JAW) } to
                ScoreCondition(0.25f, "Las capas o la textura ondulada suavizan una mandíbula ancha.", { it.layers != HairLayers.NONE || it.texture >= HairTexture.WAVY })
    )

    fun getRecommendations(faceAnalysis: FaceAnalysis): List<HairstyleRecommendation> {
        val recommendations = mutableListOf<HairstyleRecommendation>()

        // 1. Filtrar peinados por género.
        val genderFilteredHairstyles = hairstyleLabelToAttributesMap.filter { (_, attributes) ->
            if (faceAnalysis.gender == Gender.UNKNOWN) true else attributes.applicableGender.contains(faceAnalysis.gender)
        }

        // 2. Calcular puntuación para cada peinado.
        genderFilteredHairstyles.forEach { (hairstyleName, attributes) ->
            var score = 0.0f
            val currentReasons = mutableListOf<String>()

            // Aplicar reglas basadas en la forma del rostro
            faceShapeRules[faceAnalysis.faceShape]?.forEach { rule ->
                if (rule.check(attributes)) {
                    score += rule.score
                    currentReasons.add(rule.reason)
                }
            }

            // Aplicar reglas basadas en características adicionales
            featureRules.forEach { (analysisCondition, scoreCondition) ->
                if (analysisCondition(faceAnalysis) && scoreCondition.check(attributes)) {
                    score += scoreCondition.score
                    currentReasons.add(scoreCondition.reason)
                }
            }

            // 3. Normalizar puntuación y añadir a la lista si supera el umbral.
            val ruleBasedConfidence = score.coerceIn(0f, 1f)
            val symmetryAdjustmentFactor = 0.8f + (faceAnalysis.symmetryScore * 0.2f)
            val normalizedConfidence = (ruleBasedConfidence * symmetryAdjustmentFactor).coerceIn(0f, 1f)

            if (normalizedConfidence > 0.40f && currentReasons.isNotEmpty()) {
                recommendations.add(
                    HairstyleRecommendation(
                        hairstyleName = hairstyleName,
                        attributes = attributes,
                        confidence = normalizedConfidence,
                        reasons = currentReasons.distinct(), // Eliminar razones duplicadas
                        imageUrl = null // Se llenará más tarde en el ViewModel
                    )
                )
            }
        }

        // 4. Ordenar por confianza y devolver el top 5.
        return recommendations.sortedByDescending { it.confidence }.take(5)
    }

    fun getHairstyleDescription(hairstyleName: String): String {
        val attributes = hairstyleLabelToAttributesMap[hairstyleName]
        var description = "Estilo: ${hairstyleName.replace('_', ' ')}."
        attributes?.let {
            description += " Características: Largo ${it.length.name.lowercase().replace('_', ' ')}, Textura ${it.texture.name.lowercase().replace('_', ' ')}, Volumen ${it.volume.name.lowercase().replace('_', ' ')}."
            if (it.bangs != HairBangs.NONE) description += " Flequillo ${it.bangs.name.lowercase().replace('_', ' ')}."
            if (it.layers != HairLayers.NONE) description += " Capas ${it.layers.name.lowercase().replace('_', ' ')}."
            description += " Estructura general: ${it.structure.name.lowercase().replace('_', ' ')}."
        }
        return description
    }
}