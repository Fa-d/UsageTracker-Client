plugins {
    id("screentimetracker.android.feature")
}

android {
    namespace = "dev.sadakat.screentimetracker.feature.wellness"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":domain:wellness"))
    implementation(project(":domain:tracking"))
}