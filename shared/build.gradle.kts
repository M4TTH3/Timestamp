plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvm("backend")
    androidTarget("android")

    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.jackson.annotations)
        }

        val mobileMain by creating {
            dependsOn(commonMain.get())
            kotlin.srcDir("src/mobileMain/kotlin")

            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
            }
        }

        val backendMain by getting {
            dependsOn(commonMain.get())

            dependencies {
                implementation(libs.spring.boot.starter.logging)
            }
        }

        androidMain {
            dependsOn(mobileMain)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 34
    }
    namespace = "org.timestamp.shared"
}
