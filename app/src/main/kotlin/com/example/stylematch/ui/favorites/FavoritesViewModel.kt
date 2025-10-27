package com.example.stylematch.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.stylematch.data.local.entity.FavoriteHairstyleEntity
import com.example.stylematch.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- MEJORA: Anotar para que Hilt pueda inyectar dependencias.
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    // --- MEJORA: Hilt inyectará el repositorio automáticamente.
    private val favoritesRepository: FavoritesRepository
) : ViewModel() { // --- MEJORA: Cambiado a ViewModel base.

    val allFavorites: LiveData<List<FavoriteHairstyleEntity>>

    // Variable para guardar temporalmente el último favorito eliminado para la función "deshacer".
    private var lastDeletedFavorite: FavoriteHairstyleEntity? = null

    init {
        // --- MEJORA: El repositorio ya está inyectado, solo inicializamos el LiveData.
        allFavorites = favoritesRepository.getAllFavorites().asLiveData()
    }

    fun deleteFavorite(favorite: FavoriteHairstyleEntity) {
        viewModelScope.launch {
            // Guardar el favorito antes de eliminarlo de la DB para poder deshacer la acción.
            lastDeletedFavorite = favorite
            favoritesRepository.deleteFavorite(favorite)
        }
    }

    // Nueva función para restaurar el favorito eliminado.
    fun undoDelete() {
        viewModelScope.launch {
            lastDeletedFavorite?.let {
                favoritesRepository.insertFavorite(it)
                lastDeletedFavorite = null // Limpiar después de restaurar
            }
        }
    }
}