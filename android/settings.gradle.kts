import java.io.File

pluginManagement {
    plugins {
        id("com.android.application") version "8.2.1"
        id("org.jetbrains.kotlin.android") version "1.9.23"
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android"
include(":app")

val flutterProjectRoot = rootProject.projectDir.parentFile
apply(from = File(flutterProjectRoot, "packages/flutter_tools/gradle/app_plugin_loader.gradle"))
 