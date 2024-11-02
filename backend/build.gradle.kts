plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "org.timestamp"
version = "1.2.0"

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.jpa)
    implementation(libs.kotlin.reflect)
    implementation(libs.postgresql)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit5)
    testRuntimeOnly(libs.junit5.platform.launch)
}

springBoot {
    mainClass.set("org.timestamp.backend.BackendApplicationKt")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}