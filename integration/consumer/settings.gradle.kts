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
        exclusiveContent {
            forRepositories(
                maven {
                    url = uri(providers.gradleProperty("timelineRepository").get())
                    require(url.scheme == "file") { "The tested Timeline repository must be local" }
                },
                maven { url = uri("../../docs/maven") },
            )
            filter { includeModule("com.github.dmitrypokrasov", "timelineview") }
        }
        google()
        mavenCentral()
    }
}

// Device benchmarks are opt-in and require API 29+; ordinary migration checks stay API 27 compatible.
if (providers.gradleProperty("timelineBenchmarks").orNull == "true") include(":benchmark")

rootProject.name = "TimelinePublishedConsumer"
include(":legacy")
include(":compose")
