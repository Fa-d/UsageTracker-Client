pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ScreenTimeTracker"
include(":app")

// Core modules
include(":core:core-ui")
include(":core:core-network")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-common")
include(":core:core-testing")
include(":core:core-navigation")

// Data modules
include(":data:data-user")
include(":data:data-content")

// Domain modules
include(":domain:domain-user")
include(":domain:domain-content")

// Feature modules
include(":feature:feature-login")
include(":feature:feature-home")
include(":feature:feature-dashboard")
include(":feature:feature-analytics")
include(":feature:feature-wellness")
include(":feature:feature-goals")
include(":feature:feature-settings")

// Architecture enforcement
include(":architecture-rules")