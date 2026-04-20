import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.plugins.JavaPluginExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.sqldelight)
    // 10B — static analysis for generated code quality gates
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

kotlin {
    // Configure JVM toolchain for Android tasks
    jvmToolchain(17)

    // Global compiler options for all targets
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            )
        }
    }

    // Android target
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    freeCompilerArgs.addAll(
                        "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                        "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                        "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                        "-opt-in=kotlinx.coroutines.FlowPreview",
                        "-opt-in=arkivanov.decompose.ExperimentalDecomposeApi",
                        "-opt-in=app.cash.sqldelight.ExperimentalCoroutinesExtensionsApi",
                    )
                }
            }
        }
    }

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Source sets
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kotlin
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                // Ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)

                // Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Decompose
                implementation(libs.decompose)
                implementation(libs.decompose.compose)

                // Koin
                implementation(libs.koin.core)

                // SQLDelight (common extensions)
                implementation(libs.sqldelightCoroutinesExtensions)
                implementation(libs.sqldelightPrimitiveAdapters)

                // AndroidX (common)
                implementation(libs.androidx.datastore.preferences)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions)
                implementation(libs.turbine)
                // Note: mockk is JVM-only, use it in androidUnitTest only
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                // Ktor Android
                implementation(libs.ktor.client.android)

                // Compose Android
                implementation(libs.compose.activity)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.lifecycle.runtime)
                implementation(libs.compose.lifecycle.viewmodel)

                // Koin Android
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)

                // SQLDelight Android driver
                implementation(libs.sqldelightAndroidDriver)

                // AndroidX
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.lifecycle.runtime)

                // Security (EncryptedSharedPreferences)
                implementation(libs.androidx.security.crypto)

                // Biometric authentication for offline PIN/fingerprint
                implementation(libs.androidx.biometric)

                // WorkManager for background sync scheduling
                implementation(libs.androidx.work.runtime)

                // Google Maps
                implementation(libs.google.maps.compose)
                implementation(libs.google.play.services.maps)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotest.assertions)
                implementation(libs.mockk) // JVM-only
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // Ktor iOS (Darwin)
                implementation(libs.ktor.client.darwin)

                // SQLDelight iOS driver
                implementation(libs.sqldelightNativeDriver)
            }
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting

        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

// Android configuration
android {
    namespace = "id.startapp.pheromone.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    dependencies {
        // Android instrumentation tests
        androidTestImplementation(libs.androidx.test.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(libs.androidx.compose.ui.test)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
    }
}

// SQLDelight configuration
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("id.startapp.pheromone")
        }
    }
    linkSqlite = true
}

// ── 10B — Detekt configuration ───────────────────────────────────────────────

detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = false
    allRules = false
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
    )
}

// ── 10B — Generated file size gate ──────────────────────────────────────────
//
// After Phase 0–1 composition every generated Kotlin file is a thin delegator
// (~40–150 lines). This task scans all .kt files that do NOT contain the
// // <<< CUSTOM marker and fails if any exceeds MAX_GENERATED_LINES.
//
// How it catches the problem:
//   1. Developer removes // <<< CUSTOM from a generated file.
//   2. Generator re-runs and writes back the pre-composition 700-line version.
//   3. This task fails the build with a clear path + line count.
//
// The threshold is deliberately generous (250 lines) to accommodate module-level
// files (NavConfig, DeepLinks, SyncHandler) while still catching pre-composition
// bloat (670–870 lines).

tasks.register("checkGeneratedFileSizes") {
    description = "Fail if any generated .kt file exceeds the post-composition line threshold."
    group = "verification"

    val maxLines = 250
    val sourceRoot = file("src/commonMain/kotlin")

    inputs.dir(sourceRoot)

    doLast {
        var violations = 0

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { file ->
                // Only check generated files — custom files are excluded
                !file.readText().contains("// <<< CUSTOM")
            }
            .forEach { file ->
                val lineCount = file.readLines().size
                if (lineCount > maxLines) {
                    logger.error(
                        "OVERSIZED GENERATED FILE: ${file.relativeTo(projectDir)} " +
                        "has $lineCount lines (max $maxLines). " +
                        "If this file contains custom code, add '// <<< CUSTOM' to preserve it on regeneration. " +
                        "Otherwise re-run the generator with the Phase 1 composition templates."
                    )
                    violations++
                }
            }

        if (violations > 0) {
            throw GradleException(
                "$violations generated file(s) exceed the $maxLines-line threshold. " +
                "Run './gradlew :shared:checkGeneratedFileSizes' for the full list."
            )
        } else {
            logger.lifecycle("checkGeneratedFileSizes: all generated files within limit ($maxLines lines).")
        }
    }
}

// Hook into the standard check lifecycle so `./gradlew check` includes the gate
tasks.named("check") {
    dependsOn("checkGeneratedFileSizes")
}
