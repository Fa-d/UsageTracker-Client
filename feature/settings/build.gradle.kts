plugins {
    id("screentimetracker.android.feature")
}

android {
    namespace = "dev.sadakat.screentimetracker.feature.settings"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":domain:goals"))
    implementation(project(":domain:habits"))
    implementation(project(":domain:wellness"))
    implementation(project(":domain:limits"))
}