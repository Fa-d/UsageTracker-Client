plugins {
    alias(libs.plugins.screentimetracker.android.application)
}

android {
    namespace = "dev.sadakat.screentimetracker"

    defaultConfig {
        applicationId = "dev.sadakat.screentimetracker"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "dev.sadakat.screentimetracker.HiltTestRunner"
    }

    // Dynamic Feature Modules - conditionally include AI insights
    dynamicFeatures += mutableSetOf<String>().apply {
        if (project.findProject(":ai_insights") != null) {
            add(":ai_insights")
        }
    }

    // Build optimization configurations
    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = false
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.dependency.dir", "$buildDir/intermediates/exploded-aar")
            it.systemProperty("android.platforms", android.sdkDirectory.absolutePath + "/platforms")
            it.systemProperty("android.sdk", "${android.compileSdk}")
            it.environment("ANDROID_HOME", android.sdkDirectory.absolutePath)
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))

    // Feature modules
    implementation(project(":feature:dashboard"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Core Android & Compose
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Coroutines (from core:common)
    implementation(libs.bundles.coroutines)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    // Google Play Feature Delivery
    implementation(libs.play.feature.delivery)
    implementation(libs.play.feature.delivery.ktx)

    // Other libraries
    implementation(libs.gson)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.privacysandbox.tools.core)
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    // Compose testing
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)

    // Android testing
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}