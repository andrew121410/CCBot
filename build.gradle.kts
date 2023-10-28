plugins {
    `java-library`
    id("io.freefair.lombok") version "8.4" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    group = "com.andrew121410.ccbot"
    version = "1.0"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.github.johnrengelman.shadow")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}