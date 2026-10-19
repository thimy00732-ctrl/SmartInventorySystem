
plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
}

group = "com.sis"
version = "1.0.0"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:3.3.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.3.1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.3.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.3.1")
    implementation("io.ktor:ktor-server-cors-jvm:3.3.1")
    implementation("io.ktor:ktor-server-auth-jvm:3.3.1")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:3.3.1")

    // MySQL connection
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("com.mysql:mysql-connector-j:9.4.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.3.1")
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(
            org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(24)
}
