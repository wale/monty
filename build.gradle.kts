plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("kapt") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    kotlin("plugin.jpa") version "1.8.0"
    id("io.ebean") version "13.15.0"
    application
}

group = "au.id.wale"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.chew.pro/snapshots")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("net.dv8tion:JDA:5.0.0-beta.6")
    implementation("com.github.Devoxin:Flight:b692e6033d")
    implementation("ch.qos.logback:logback-classic:1.4.6")

    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")

    implementation("com.sksamuel.hoplite:hoplite-core:2.7.3")
    implementation("com.sksamuel.hoplite:hoplite-toml:2.7.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("io.ebean:ebean:13.15.0")
    implementation("io.ebean:ebean-test:13.15.0") {
        exclude(group = "com.h2database", module = "h2")
    }
    implementation("io.ebean:ebean-migration:13.7.0")

    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.json:json:20230227")

    kapt("io.ebean:kotlin-querybean-generator:13.15.0")

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("au.id.wale.monty.MainKt")
}