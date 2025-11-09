plugins {
    `maven-publish`
}

description = "CCBot-Core"

dependencies {
    api("net.dv8tion:JDA:6.1.1")   // https://mvnrepository.com/artifact/net.dv8tion/JDA
    api("org.reflections:reflections:0.10.2") // https://mvnrepository.com/artifact/org.reflections/reflections
    api("com.github.andrew121410.CCUtilsJava:ccutilsjava-relocation:08b1571b50")
    api("com.github.andrew121410:minecraft-server-ping:0756645bb6")

    // Jackson -> https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    val jacksonVersion = "2.20.1"
    api("com.fasterxml.jackson.core:jackson-annotations:3.0-rc5")
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}