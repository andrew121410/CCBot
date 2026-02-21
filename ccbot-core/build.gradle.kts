plugins {
    `maven-publish`
}

description = "CCBot-Core"

dependencies {
    api("net.dv8tion:JDA:6.3.1")   // https://mvnrepository.com/artifact/net.dv8tion/JDA
    api("org.reflections:reflections:0.10.2") // https://mvnrepository.com/artifact/org.reflections/reflections
    api("com.github.andrew121410.CCUtilsJava:ccutilsjava-relocation:2a3b65a477")
    api("com.github.andrew121410:minecraft-server-ping:6cc532c374")

    // Jackson -> https://mvnrepository.com/artifact/tools.jackson.core/jackson-core
    val jacksonVersion = "3.1.0-rc1"
    api("tools.jackson.core:jackson-core:$jacksonVersion")
    api("tools.jackson.core:jackson-databind:$jacksonVersion")
    api("tools.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}
