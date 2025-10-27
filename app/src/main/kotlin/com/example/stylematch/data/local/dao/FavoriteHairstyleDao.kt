package com.example.stylematch.data.local.dao // Nuevo paquete para DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stylematch.data.local.entity.FavoriteHairstyleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteHairstyleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteHairstyleEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteHairstyleEntity)

    @Query("DELETE FROM favorite_hairstyles WHERE hairstyleName = :hairstyleName")
    suspend fun deleteFavoriteByName(hairstyleName: String)

    @Query("SELECT * FROM favorite_hairstyles WHERE hairstyleName = :hairstyleName")
    suspend fun getFavoriteByName(hairstyleName: String): FavoriteHairstyleEntity?

    @Query("SELECT * FROM favorite_hairstyles ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteHairstyleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_hairstyles WHERE hairstyleName = :hairstyleName LIMIT 1)")
    fun isFavorite(hairstyleName: String): Flow<Boolean>
}