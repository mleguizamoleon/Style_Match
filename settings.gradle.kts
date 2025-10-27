pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // <--- ¡Asegúrate de que esta línea exista!
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StyleMatch"
include(":app")