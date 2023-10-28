description = "ccbot-bukkit"

dependencies {
    api(project(":ccbot-core"))
    compileOnly("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("CCBot")
        archiveClassifier.set("")
        archiveVersion.set("")

        exclude("META-INF/*.MF", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}