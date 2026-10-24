plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
    jacoco
}

group = "com.github.dmitrypokrasov"
version = "2.2.0"

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
    testFixtures.enable = true
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
            // JUnit остаётся в metadata testFixtures, но не в POM основного артефакта.
            pom.withXml {
                val dependencyNodes = asNode().get("dependencies") as groovy.util.NodeList
                val dependencies = dependencyNodes.firstOrNull() as? groovy.util.Node
                dependencies?.children()?.filterIsInstance<groovy.util.Node>()
                    ?.filter { (it.get("groupId") as groovy.util.NodeList).text() == "junit" }
                    ?.forEach { dependencies.remove(it) }
            }
            pom {
                name.set("Timeline")
                description.set("Android timeline widget with pluggable layout and rendering strategies")
                url.set("https://github.com/dmitrypokrasov/Timeline")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/dmitrypokrasov/Timeline")
                    connection.set("scm:git:https://github.com/dmitrypokrasov/Timeline.git")
                }
            }
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
    // Lottie uses AppCompat; retain the verified version instead of its older transitive default.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.customview)
    implementation(libs.lottie)
    testFixturesApi(libs.junit)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Отчёт покрытия задаёт baseline без искусственного общего порога.
jacoco { toolVersion = "0.8.12" }
tasks.withType<Test>().configureEach {
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}
tasks.register<JacocoReport>("coverageReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    executionData(fileTree(layout.buildDirectory) { include("jacoco/testDebugUnitTest.exec") })
    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
            exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*")
        },
    )
}
