package com.example.stylematch.di

import android.content.Context
import com.example.stylematch.data.local.AppDatabase
import com.example.stylematch.data.local.dao.FavoriteHairstyleDao
import com.example.stylematch.data.remote.PexelsApiService
import com.example.stylematch.data.remote.RetrofitClient
import com.example.stylematch.ml.FaceAnalyzer
import com.example.stylematch.ml.FaceShapeClassifier
import com.example.stylematch.ml.HairstyleRecommender
import com.example.stylematch.repository.FavoritesRepository
import com.example.stylematch.repository.HairstyleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideFavoriteHairstyleDao(database: AppDatabase): FavoriteHairstyleDao {
        return database.favoriteHairstyleDao()
    }

    @Provides
    @Singleton
    fun providePexelsApiService(): PexelsApiService {
        // Tu RetrofitClient ya sigue un patrón Singleton, así que lo reutilizamos.
        return RetrofitClient.instance
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(dao: FavoriteHairstyleDao): FavoritesRepository {
        // Hilt ahora sabe cómo crear 'dao', por lo que puede crear este repositorio.
        return FavoritesRepository(dao)
    }

    @Provides
    @Singleton
    fun provideHairstyleRepository(apiService: PexelsApiService): HairstyleRepository {
        // Hilt también sabe cómo crear 'apiService'.
        return HairstyleRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideFaceShapeClassifier(@ApplicationContext context: Context): FaceShapeClassifier {
        return FaceShapeClassifier(context)
    }

    // Provee el FaceAnalyzer, que ahora dependerá del FaceShapeClassifier.
    // Hilt inyectará automáticamente el clasificador que proveímos arriba.
    @Provides
    @Singleton
    fun provideFaceAnalyzer(
        @ApplicationContext context: Context,
        faceShapeClassifier: FaceShapeClassifier
    ): FaceAnalyzer {
        return FaceAnalyzer(context, faceShapeClassifier)
    }

    @Provides
    @Singleton
    fun provideHairstyleRecommender(): HairstyleRecommender {
        return HairstyleRecommender()
    }
}