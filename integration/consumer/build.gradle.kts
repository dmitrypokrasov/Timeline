plugins {
    id("com.android.application") version "9.4.1"
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    namespace = "com.example.consumer"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.consumer"
        minSdk = 27
        targetSdk = 34
        testInstrumentationRunner = "com.example.consumer.MinifiedConsumerInstrumentation"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Test-only consumer APK; never distributed as a production application.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    testBuildType = "release"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    sourceSets["main"].java.srcDir("../migration/after")
    sourceSets["main"].res.srcDir("../migration/res")
    sourceSets["test"].java.srcDir("../migration/test")
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2") }
    }
}

dependencies {
    implementation("com.github.dmitrypokrasov:timelineview:${providers.gradleProperty("timelineVersion").get()}")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")
}
