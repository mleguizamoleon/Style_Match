package com.example.stylematch.repository

import android.util.Log
import com.example.stylematch.BuildConfig
import com.example.stylematch.data.model.pexel.PexelsPhoto
import com.example.stylematch.data.remote.PexelsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// --- MEJORA: Anotar como Singleton y convertir a clase para inyección.
@Singleton
class HairstyleRepository @Inject constructor(
    // --- MEJORA: Hilt inyectará el servicio de la API automáticamente.
    private val pexelsApiService: PexelsApiService
) {
    private val TAG = "HairstyleRepository"
    private val apiKeyFromBuildConfig: String = BuildConfig.PEXELS_API_KEY //

    suspend fun searchHairstyles(query: String, page: Int, perPage: Int = 30): Result<List<PexelsPhoto>> {
        if (apiKeyFromBuildConfig.isBlank() || apiKeyFromBuildConfig == "\"\"") {
            val errorMsg = "La clave API de Pexels no está configurada."
            Log.e(TAG, errorMsg)
            return Result.failure(Exception(errorMsg))
        }
        val actualApiKey = apiKeyFromBuildConfig.removeSurrounding("\"")
        if (actualApiKey.isBlank()) {
            val errorMsg = "La clave API de Pexels es inválida."
            Log.e(TAG, errorMsg)
            return Result.failure(Exception(errorMsg))
        }

        return withContext(Dispatchers.IO) {
            try {
                // Se pasa el número de página a la llamada de la API.
                val response = pexelsApiService.searchPhotos(
                    apiKey = actualApiKey,
                    query = query,
                    page = page,
                    perPage = perPage
                )
                if (response.isSuccessful) {
                    response.body()?.photos?.let { photos ->
                        Log.d(TAG, "Se obtuvieron ${photos.size} fotos para la consulta: '$query' en la página $page")
                        Result.success(photos)
                    } ?: Result.failure(Exception("Cuerpo de respuesta vacío desde la API de Pexels."))
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: "Cuerpo de error no disponible"
                    val errorMsg = "Error al obtener peinados: ${response.code()} - ${response.message()}. Cuerpo: $errorBodyString"
                    Log.e(TAG, errorMsg) //
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error de red u otro al obtener peinados: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}