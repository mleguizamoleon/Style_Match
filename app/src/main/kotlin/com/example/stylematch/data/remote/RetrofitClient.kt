package com.example.stylematch.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.stylematch.BuildConfig // Importa BuildConfig de tu módulo
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.pexels.com/"
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        val context = applicationContext
            ?: throw IllegalStateException("RetrofitClient context not initialized for network check.")

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network)?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("OkHttpPexels", message) // Puedes usar un TAG específico
    }.apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient: OkHttpClient by lazy {
        if (applicationContext == null) {
            throw IllegalStateException("RetrofitClient must be initialized with Context before accessing OkHttpClient.")
        }
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB de caché
        val httpCacheDirectory = File(applicationContext!!.cacheDir, "http-cache")
        val cache = Cache(httpCacheDirectory, cacheSize)

        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                var request = chain.request()
                if (!isNetworkAvailable()) {
                    val cacheControl = CacheControl.Builder()
                        .maxStale(7, TimeUnit.DAYS)
                        .build()
                    request = request.newBuilder()
                        .removeHeader("Pragma")
                        .cacheControl(cacheControl)
                        .build()
                    Log.d("RetrofitClient", "Network unavailable, using cache (max-stale 7 days).")
                }
                chain.proceed(request)
            }
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                val cacheControl = CacheControl.Builder()
                    .maxAge(24, TimeUnit.HOURS)
                    .build()
                response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", cacheControl.toString())
                    .build()
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val instance: PexelsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsApiService::class.java)
    }
}