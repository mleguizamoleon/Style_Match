package com.example.stylematch.ui.analysis

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylematch.data.local.entity.FavoriteHairstyleEntity
import com.example.stylematch.ml.FaceAnalyzer
import com.example.stylematch.ml.HairstyleRecommender
import com.example.stylematch.repository.FavoritesRepository
import com.example.stylematch.repository.HairstyleRepository
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// --- MEJORA: Anotar para que Hilt pueda inyectar dependencias.
@HiltViewModel
class FaceAnalysisViewModel @Inject constructor(
    // --- MEJORA: Todas las dependencias son inyectadas por Hilt.
    private val faceAnalyzer: FaceAnalyzer,
    private val hairstyleRecommender: HairstyleRecommender,
    private val favoritesRepository: FavoritesRepository,
    private val hairstyleRepository: HairstyleRepository
) : ViewModel() { // --- MEJORA: Cambiado a ViewModel base.

    private val _analysisState = MutableLiveData<AnalysisState>()
    val analysisState: LiveData<AnalysisState> = _analysisState

    private val _favoriteToggledEvent = MutableSharedFlow<Pair<String, Boolean>>()
    val favoriteToggledEvent = _favoriteToggledEvent.asSharedFlow()

    // --- MEJORA: El bloque init para crear dependencias ya no es necesario.

    fun setLoadingState() {
        _analysisState.value = AnalysisState.Loading
    }

    fun analyzeFaceAndRecommend(face: Face, faceBitmap: Bitmap) {
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Loading
            try {
                val faceAnalysisResult = withContext(Dispatchers.Default) {
                    faceAnalyzer.analyzeFace(face, faceBitmap)
                }
                val initialRecommendations = withContext(Dispatchers.Default) {
                    hairstyleRecommender.getRecommendations(faceAnalysisResult)
                }
                // Enriquece las recomendaciones con imágenes.
                val enrichedRecommendations = enrichRecommendationsWithImages(initialRecommendations, faceAnalysisResult.gender)
                _analysisState.value = AnalysisState.Success(faceAnalysisResult, enrichedRecommendations)
            } catch (e: Exception) {
                Log.e("FaceAnalysisVM", "Error en análisis/recomendación: ${e.message}", e)
                _analysisState.value = AnalysisState.Error(e.message ?: "Ocurrió un error durante el análisis.")
            }
        }
    }

    private suspend fun enrichRecommendationsWithImages(
        recommendations: List<HairstyleRecommender.HairstyleRecommendation>,
        gender: FaceAnalyzer.Gender
    ): List<HairstyleRecommender.HairstyleRecommendation> = coroutineScope {
        val imageFetchJobs = recommendations.map { recommendation ->
            async(Dispatchers.IO) {
                val genderString = if (gender == FaceAnalyzer.Gender.MALE) "man" else "woman"
                val hairstyleName = recommendation.hairstyleName.replace('_', ' ')
                // Query de búsqueda más precisa para mejores resultados de imagen.
                val query = "$genderString with $hairstyleName hairstyle portrait"
                Log.d("FaceAnalysisVM", "Pexels query: '$query'")
                val result = hairstyleRepository.searchHairstyles(query = query, page = 1, perPage = 1)
                result.fold(
                    onSuccess = { photos -> photos.firstOrNull()?.src?.medium },
                    onFailure = {
                        Log.e("FaceAnalysisVM", "Error buscando imagen para ${recommendation.hairstyleName}: ${it.message}")
                        null
                    }
                )
            }
        }
        val imageUrls = imageFetchJobs.awaitAll()
        recommendations.forEachIndexed { index, recommendation ->
            recommendation.imageUrl = imageUrls[index]
        }
        return@coroutineScope recommendations
    }

    fun getHairstyleDescription(hairstyleType: String): String {
        return hairstyleRecommender.getHairstyleDescription(hairstyleType)
    }

    fun isHairstyleFavorite(hairstyleName: String): Flow<Boolean> {
        return favoritesRepository.isFavorite(hairstyleName)
    }

    fun toggleFavoriteStatus(
        recommendation: HairstyleRecommender.HairstyleRecommendation,
        faceAnalysis: FaceAnalyzer.FaceAnalysis,
        isCurrentlyFavorite: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val hairstyleName = recommendation.hairstyleName
            if (isCurrentlyFavorite) {
                favoritesRepository.deleteFavoriteByName(hairstyleName)
            } else {
                val favoriteEntity = FavoriteHairstyleEntity(
                    hairstyleName = hairstyleName,
                    description = getHairstyleDescription(hairstyleName),
                    mainReason = recommendation.reasons.firstOrNull() ?: "Compatible",
                    confidenceAtRecommendation = recommendation.confidence,
                    faceShapeAtRecommendation = "${faceAnalysis.faceShape.name} (${faceAnalysis.gender.name})",
                    timestamp = System.currentTimeMillis(),
                    imageUrl = recommendation.imageUrl // Asegurarse de que la URL de la imagen se guarde
                )
                favoritesRepository.insertFavorite(favoriteEntity)
            }
            _favoriteToggledEvent.emit(hairstyleName to !isCurrentlyFavorite)
        }
    }

    fun reportCameraInitializationError() {
        _analysisState.value = AnalysisState.Error("No se pudo iniciar la cámara.")
    }

    fun reportGalleryImageError(errorMessage: String) {
        _analysisState.value = AnalysisState.Error(errorMessage)
    }
}