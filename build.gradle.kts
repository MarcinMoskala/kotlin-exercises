@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20-RC2"
    kotlin("plugin.power-assert") version "2.2.20-RC2"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("test"))
    implementation(kotlin("test-junit"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    implementation("com.sksamuel.aedile:aedile-core:2.1.2") // Needed for CompanyDetailsRepository
    implementation("io.ktor:ktor-client-core:3.0.2")
    implementation("io.ktor:ktor-client-java:3.0.2")
    implementation("io.ktor:ktor-client-websockets:3.0.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn.add("kotlinx.coroutines.ExperimentalAtomicApi")
        optIn.add("kotlinx.coroutines.ExperimentalUuidApi")
//        freeCompilerArgs.add("-Xdebug")
    }
}

java.sourceSets["test"].java {
    srcDir("src/main/kotlin")
}

powerAssert {
    functions = listOf("kotlin.assert", "kotlin.test.assertEquals")
}

// Uncomment to use Loom
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")

}

//kotlin {
//    jvmToolchain(20)
//}
