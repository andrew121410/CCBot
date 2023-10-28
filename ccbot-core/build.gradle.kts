plugins {
    `maven-publish`
}

description = "CCBot-Core"

dependencies {
    api("net.dv8tion:JDA:5.0.0-beta.16")   // https://mvnrepository.com/artifact/net.dv8tion/JDA
    api("org.reflections:reflections:0.10.2") // https://mvnrepository.com/artifact/org.reflections/reflections
    api("com.github.andrew121410:CCUtilsJava:0cad94ef")
    api("com.github.andrew121410:minecraft-server-ping:b1d8c960")

    // Jackson
    api("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    api("com.fasterxml.jackson.core:jackson-core:2.15.2")
    api("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
}