group = "nl.koenhabets.location-recorder.server"
version = "1.0"

val kotlin_version: String by project
val ktor_version: String by project
val embed_web: String by project

plugins {
    application
    kotlin("jvm") version "1.5.30"
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.4")
    implementation("org.json:json:20210307")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.register<Copy>("copyWeb") {
    dependsOn(":web:build")
    from("$rootDir/web/dist")
    into("$rootDir/server/build/resources/main/static")
}

if (embed_web == "true") {
    tasks.named("processResources") {
        dependsOn("copyWeb")
    }
}
