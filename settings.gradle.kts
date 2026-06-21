pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "KrishiFarms"

include(":app")

// Future multi-module expansion:
// include(":core:common")
// include(":core:network")
// include(":core:database")
// include(":core:ui")
// include(":feature:auth")
// include(":feature:dashboard")
// ... additional feature modules
