plugins {
    id("screentimetracker.android.library")
    id("screentimetracker.android.hilt")
}

android {
    namespace = "dev.sadakat.screentimetracker.domain.goals"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}