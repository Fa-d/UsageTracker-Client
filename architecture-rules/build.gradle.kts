plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.5")
    implementation("io.gitlab.arturbosch.detekt:detekt-cli:1.23.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.5")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}