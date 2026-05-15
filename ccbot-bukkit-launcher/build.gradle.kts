description = "ccbot-bukkit"

dependencies {
    api(project(":ccbot-core"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.63-stable")
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