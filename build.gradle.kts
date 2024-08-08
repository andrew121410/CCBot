plugins {
    `java-library`
    id("io.freefair.lombok") version "8.7.1" apply false // https://plugins.gradle.org/plugin/io.freefair.lombok
//    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.github.goooler.shadow") version "8.1.8" // https://github.com/johnrengelman/shadow/pull/876 https://github.com/Goooler/shadow https://plugins.gradle.org/plugin/io.github.goooler.shadow
}

allprojects {
    group = "com.andrew121410.ccbot"
    version = "1.0"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "io.github.goooler.shadow")

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