plugins {
    `maven-publish`
}

description = "CCBot-Core"

dependencies {
    api("net.dv8tion:JDA:5.3.0")   // https://mvnrepository.com/artifact/net.dv8tion/JDA
    api("org.reflections:reflections:0.10.2") // https://mvnrepository.com/artifact/org.reflections/reflections
    api("com.github.andrew121410.CCUtilsJava:ccutilsjava-relocation:a96019572c")
    api("com.github.andrew121410:minecraft-server-ping:6b0ea96f9f")

    // Jackson -> https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    val jacksonVersion = "2.18.3"
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}