plugins {
    alias(libs.plugins.screentimetracker.android.library)
    alias(libs.plugins.screentimetracker.android.hilt)
    id("com.google.devtools.ksp")
}

android {
    namespace = "dev.sadakat.screentimetracker.core.database"
}

dependencies {
    // Core modules
    implementation(project(":core:common"))

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // JSON
    implementation(libs.gson)

    // Testing
    testImplementation(libs.bundles.testing.unit)
    testImplementation(libs.androidx.room.compiler)
    androidTestImplementation(libs.bundles.testing.android)
}