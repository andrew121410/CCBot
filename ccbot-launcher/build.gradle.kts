description = "ccbot-launcher"

dependencies {
    api(project(":ccbot-core"))
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    jar {
        enabled = false
    }

    shadowJar {
        manifest {
            attributes["Main-Class"] = "com.andrew121410.ccbot.CCBot"
        }

        archiveBaseName.set("CCBot")
        archiveClassifier.set("")
        archiveVersion.set("")

        exclude("META-INF/*.MF", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}