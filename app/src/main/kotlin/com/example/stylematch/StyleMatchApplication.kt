package com.example.stylematch

import android.app.Application
import android.util.Log
import com.example.stylematch.data.remote.RetrofitClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StyleMatchApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("StyleMatchApplication", "Application Created and Hilt Initialized")

        // Inicializar RetrofitClient con el contexto de la aplicación.
        // Esto es útil para la caché de red que implementaste.
        RetrofitClient.initialize(applicationContext)
        Log.d("StyleMatchApplication", "RetrofitClient initialized for caching.")
    }
}