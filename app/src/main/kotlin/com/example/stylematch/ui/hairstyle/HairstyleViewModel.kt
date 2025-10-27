package com.example.stylematch.ui.hairstyle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylematch.data.model.pexel.PexelsPhoto
import com.example.stylematch.repository.HairstyleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HairstyleUIState {
    object InitialLoading : HairstyleUIState()
    data class Success(val photos: List<PexelsPhoto>, val isLoadingMore: Boolean) : HairstyleUIState()
    data class Error(val message: String) : HairstyleUIState()
}

// --- MEJORA: Anotar para que Hilt pueda inyectar dependencias.
@HiltViewModel
class HairstyleViewModel @Inject constructor(
    // --- MEJORA: Hilt inyectará el repositorio automáticamente.
    private val repository: HairstyleRepository
) : ViewModel() { // --- MEJORA: Cambiado a ViewModel base.

    private val _hairstyleUIState = MutableLiveData<HairstyleUIState>()
    val hairstyleUIState: LiveData<HairstyleUIState> = _hairstyleUIState

    private var currentPage = 1
    var currentQuery = ""
    private var currentPhotoList = mutableListOf<PexelsPhoto>()
    var isFetching = false
    private var isLastPage = false

    fun fetchHairstyles(query: String) {
        if (isFetching || isLastPage) return

        viewModelScope.launch {
            isFetching = true
            // Si es una nueva búsqueda, reinicia el estado.
            if (query != currentQuery) {
                currentPage = 1
                currentQuery = query
                currentPhotoList.clear()
                isLastPage = false
                _hairstyleUIState.value = HairstyleUIState.InitialLoading
            } else { // Si no, es para cargar más (paginación).
                _hairstyleUIState.value = HairstyleUIState.Success(currentPhotoList, isLoadingMore = true)
            }

            val result = repository.searchHairstyles(query = currentQuery, page = currentPage)
            result.fold(
                onSuccess = { newPhotos ->
                    if (newPhotos.isNotEmpty()) {
                        // Añade las nuevas fotos a la lista existente.
                        currentPhotoList.addAll(newPhotos)
                        currentPage++
                    } else {
                        // Si la API no devuelve más fotos, marcamos que es la última página.
                        isLastPage = true
                    }
                    _hairstyleUIState.value = HairstyleUIState.Success(currentPhotoList, isLoadingMore = false)
                },
                onFailure = { exception ->
                    _hairstyleUIState.value = HairstyleUIState.Error(exception.message ?: "Ocurrió un error desconocido")
                }
            )
            isFetching = false
        }
    }
}