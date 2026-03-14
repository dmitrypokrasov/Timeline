import com.android.build.api.dsl.CommonExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.binary.compatibility.validator) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.ktlint) apply false
}

fun Project.configureAndroidQuality() {
    extensions.configure<CommonExtension<*, *, *, *, *, *>>("android") {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        lint {
            abortOnError = true
            checkReleaseBuilds = false
            explainIssues = true
            htmlReport = true
            warningsAsErrors = false
            xmlReport = true
            lintConfig = rootProject.file("lint.xml")
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

subprojects {
    pluginManager.withPlugin("com.android.application") {
        configureAndroidQuality()
    }
    pluginManager.withPlugin("com.android.library") {
        configureAndroidQuality()
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
        apply(plugin = "io.gitlab.arturbosch.detekt")
        apply(plugin = "org.jetbrains.dokka")

        extensions.configure<KtlintExtension> {
            android.set(true)
            outputToConsole.set(true)
            ignoreFailures.set(false)
            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            allRules = false
            parallel = true
            config.setFrom(rootProject.files("detekt.yml"))

            val baselineFile = project.file("detekt-baseline.xml")
            baseline = baselineFile.takeIf { it.exists() }
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget = "1.8"
            reports {
                html.required.set(true)
                xml.required.set(true)
                sarif.required.set(true)
                txt.required.set(false)
                md.required.set(false)
            }
        }

        plugins.withType<JavaBasePlugin> {
            tasks.named("check").configure {
                dependsOn("ktlintCheck", "detekt")
            }
        }
    }
}

tasks.register("qualityCheck") {
    description = "Runs formatting checks, static analysis, lint, and unit tests for all modules."
    group = "verification"
    dependsOn(
        ":app:ktlintCheck",
        ":app:detekt",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":timelineview:ktlintCheck",
        ":timelineview:detekt",
        ":timelineview:lintDebug",
        ":timelineview:testDebugUnitTest",
        ":timelineview:apiCheck"
    )
}

tasks.register("qualityFormat") {
    description = "Formats Kotlin sources with ktlint."
    group = "formatting"
    dependsOn(":app:ktlintFormat", ":timelineview:ktlintFormat")
}

tasks.register("qualityDocs") {
    description = "Generates API documentation for the library module."
    group = "documentation"
    dependsOn(":timelineview:dokkaHtml")
}
