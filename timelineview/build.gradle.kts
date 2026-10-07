plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

group = "com.github.dmitrypokrasov"
version = "2.0.0"

android {
    namespace = "com.dmitrypokrasov.timelineview"
    compileSdk = 34

    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
            it.maxHeapSize = "2g"
            it.inputs.dir("src/test/golden")
            it.systemProperty("timeline.updateGoldens", providers.gradleProperty("updateGoldens").orElse("false").get())
            if (!providers.gradleProperty("includeBenchmarks").isPresent) it.filter.excludeTestsMatching("*TimelinePerformanceTest")
            if (providers.gradleProperty("skipScreenshots").isPresent) it.filter.excludeTestsMatching("*TimelineScreenshotTest")
        }
    }
    publishing {
        singleVariant("release") { withSourcesJar() }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            artifactId = "timelineview"
        }
    }
    repositories {
        maven {
            name = "Build"
            url = uri(rootProject.layout.buildDirectory.dir("repository"))
        }
        maven {
            name = "GitHubPages"
            url = uri(rootProject.layout.projectDirectory.dir("docs/maven"))
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dmitrypokrasov/Timeline")
            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.customview)
    implementation(libs.material)
    implementation(libs.lottie)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
