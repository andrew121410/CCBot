plugins {
    `maven-publish`
}

description = "CCBot-Core"

dependencies {
    api("net.dv8tion:JDA:5.0.0-beta.24")   // https://mvnrepository.com/artifact/net.dv8tion/JDA
    api("org.reflections:reflections:0.10.2") // https://mvnrepository.com/artifact/org.reflections/reflections
    api("com.github.andrew121410:CCUtilsJava:cbafc81f3e")
    api("com.github.andrew121410:minecraft-server-ping:4b8966e91d")

    // Jackson -> https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    val jacksonVersion = "2.17.1"
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}