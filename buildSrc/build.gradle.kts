plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.10.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.20-1.0.31")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.1.20")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "screentimetracker.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "screentimetracker.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "screentimetracker.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "screentimetracker.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("androidHilt") {
            id = "screentimetracker.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}