package com.example.stylematch.data.remote

import com.example.stylematch.data.model.pexel.PexelsSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsApiService {
    @GET("v1/search") // Endpoint for searching photos
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 30, // Default to 30 images per page
        @Query("page") page: Int = 1
    ): Response<PexelsSearchResponse>
}