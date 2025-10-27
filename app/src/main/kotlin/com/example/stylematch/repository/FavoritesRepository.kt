package com.example.stylematch.repository

import com.example.stylematch.data.local.dao.FavoriteHairstyleDao
import com.example.stylematch.data.local.entity.FavoriteHairstyleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// --- MEJORA: Anotar como Singleton para que Hilt gestione una única instancia.
@Singleton
class FavoritesRepository @Inject constructor(
    // --- MEJORA: Hilt inyectará el DAO automáticamente.
    private val favoriteHairstyleDao: FavoriteHairstyleDao
) {

    fun getAllFavorites(): Flow<List<FavoriteHairstyleEntity>> {
        return favoriteHairstyleDao.getAllFavorites()
    }

    suspend fun insertFavorite(favorite: FavoriteHairstyleEntity) {
        withContext(Dispatchers.IO) {
            favoriteHairstyleDao.insertFavorite(favorite)
        }
    }

    suspend fun deleteFavorite(favorite: FavoriteHairstyleEntity) {
        withContext(Dispatchers.IO) {
            favoriteHairstyleDao.deleteFavorite(favorite)
        }
    }

    suspend fun deleteFavoriteByName(hairstyleName: String) {
        withContext(Dispatchers.IO) {
            favoriteHairstyleDao.deleteFavoriteByName(hairstyleName)
        }
    }

    suspend fun getFavoriteByName(hairstyleName: String): FavoriteHairstyleEntity? {
        return withContext(Dispatchers.IO) {
            favoriteHairstyleDao.getFavoriteByName(hairstyleName)
        }
    }

    fun isFavorite(hairstyleName: String): Flow<Boolean> {
        return favoriteHairstyleDao.isFavorite(hairstyleName)
    }
}