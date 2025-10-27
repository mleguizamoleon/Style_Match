package com.example.stylematch.data.model.pexel

import com.google.gson.annotations.SerializedName

data class PexelsSearchResponse(
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("photos") val photos: List<PexelsPhoto>,
    @SerializedName("next_page") val nextPage: String?
)