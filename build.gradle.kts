plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)

    alias(libs.plugins.spring.framework.boot)
}

group = "ru.makcpp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.spring.boot.starter.validation)

    implementation(libs.telegrambots.springboot.longpolling.starter)
    implementation(libs.telegrambots.client)
}

kotlin {
    jvmToolchain(21)
}