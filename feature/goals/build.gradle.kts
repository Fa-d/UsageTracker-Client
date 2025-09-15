plugins {
    id("screentimetracker.android.feature")
}

android {
    namespace = "dev.sadakat.screentimetracker.feature.goals"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":domain:goals"))
    implementation(project(":domain:tracking"))
}