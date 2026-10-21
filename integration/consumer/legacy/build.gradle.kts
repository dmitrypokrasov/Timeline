import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
}
extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.example.migration.legacy"
    compileSdk = 34
    defaultConfig { minSdk = 27 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets.named("main") { kotlin.directories.add("../../migration/before") }
}
dependencies { implementation("com.github.dmitrypokrasov:timelineview:1.1.0") }

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        jvmDefault.set(JvmDefaultMode.DISABLE)
    }
}
