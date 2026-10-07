plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.timelinecompose"
    compileSdk = 34
    defaultConfig { minSdk = 27 }
    buildFeatures { compose = true }
    // The repository uses Kotlin 1.9.0; this compiler pairing is intentionally explicit.
    composeOptions { kotlinCompilerExtensionVersion = "1.5.2" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2") }
    }
}

dependencies {
    implementation("com.github.dmitrypokrasov:timelineview:${providers.gradleProperty("timelineVersion").get()}")
    implementation("androidx.compose.ui:ui:1.5.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")
}
