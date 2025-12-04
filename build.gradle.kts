plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "ru.makcpp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    jvmToolchain(21)
}