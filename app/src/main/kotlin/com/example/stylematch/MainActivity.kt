package com.example.stylematch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.stylematch.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

// --- MEJORA: Anotación para habilitar la inyección de Hilt en la Activity y sus Fragments.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        // No se necesita la lógica de botones aquí, ya que está en MainMenuFragment.
    }

    // Opcional: si usas ActionBar y quieres manejar el botón "up".
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}