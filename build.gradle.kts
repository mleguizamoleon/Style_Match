// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // AGP version aligned with libs.versions.toml
        classpath("com.android.tools.build:gradle:${libs.versions.agp.get()}")
        // --- MEJORA: Añadir classpath para Hilt ---
        classpath("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
    }
    // Forzar la versión de AGP
    configurations.all {
        resolutionStrategy {
            force("com.android.tools.build:gradle:${libs.versions.agp.get()}")
            force("com.android.tools.build:builder:${libs.versions.agp.get()}")
        }
    }
}

plugins {
    // Apply false for these plugins as they are applied in modules
    // Versions are sourced from libs.versions.toml via aliases
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    // --- MEJORA: Añadir plugin de Hilt (aplicado en el módulo de la app) ---
    alias(libs.plugins.hilt.android) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}