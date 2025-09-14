plugins {
    alias(libs.plugins.screentimetracker.android.library)
}

android {
    namespace = "dev.sadakat.screentimetracker.core.common"
}

dependencies {
    api(libs.bundles.coroutines)
    api(libs.gson)
    api(libs.kotlinx.datetime)

    testImplementation(libs.bundles.testing.unit)
}