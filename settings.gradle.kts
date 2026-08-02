import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
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

rootProject.name = "fintech-mobile-patterns-android"

include(":app")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:security")
include(":core:ui")
include(":domain")
include(":data")
include(":feature:payment")
include(":feature:cards")
include(":feature:transactions")
