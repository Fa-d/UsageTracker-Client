plugins {
    id("screentimetracker.android.feature")
}

android {
    namespace = "dev.sadakat.screentimetracker.feature.focus"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":domain:habits"))
    implementation(project(":domain:tracking"))
    implementation(project(":domain:goals"))
}