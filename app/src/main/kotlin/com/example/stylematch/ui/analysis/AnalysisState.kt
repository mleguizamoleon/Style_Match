package com.example.stylematch.ui.analysis

import com.example.stylematch.ml.FaceAnalyzer
import com.example.stylematch.ml.HairstyleRecommender

sealed class AnalysisState {
    object Loading : AnalysisState()
    data class Success(
        val analysisResult: FaceAnalyzer.FaceAnalysis,
        val recommendations: List<HairstyleRecommender.HairstyleRecommendation>
    ) : AnalysisState()
    data class Error(val errorMessage: String) : AnalysisState()
} 