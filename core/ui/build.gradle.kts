plugins {
    alias(libs.plugins.screentimetracker.android.library)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.sadakat.screentimetracker.core.ui"

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    // Core modules
    api(project(":core:common"))

    // Compose BOM and core libraries
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
    api(libs.androidx.compose.material.icons.extended)

    // Activity Compose
    api(libs.androidx.activity.compose)

    // Lifecycle
    api(libs.bundles.lifecycle)

    // Navigation (for shared navigation components)
    api(libs.androidx.navigation.compose)

    // Image loading
    api(libs.bundles.coil)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}