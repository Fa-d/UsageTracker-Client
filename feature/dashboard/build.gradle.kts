plugins {
    alias(libs.plugins.screentimetracker.android.feature)
}

android {
    namespace = "dev.sadakat.screentimetracker.feature.dashboard"
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))

    // Testing
    testImplementation(libs.bundles.testing.unit)
    androidTestImplementation(libs.bundles.testing.android)
}