plugins {
    `java-library`
    id("io.freefair.lombok") version "8.12.1" apply false // https://plugins.gradle.org/plugin/io.freefair.lombok
    id("com.gradleup.shadow") version "8.3.6" // https://github.com/GradleUp/shadow
}

allprojects {
    group = "com.andrew121410.ccbot"
    version = "1.0"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            url = uri("https://jitpack.io")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}