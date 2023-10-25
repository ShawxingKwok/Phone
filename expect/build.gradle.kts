plugins {
    alias(libs.plugins.kt.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.server.core)
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-websockets:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-websockets-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-cors-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")
    // implementation("ch.qos.logback:logback-classic:1.4.11")

    testImplementation ("io.ktor:ktor-server-tests-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
    implementation(kotlin("test"))
    implementation("io.ktor:ktor-client-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
}

dependencies {
    implementation(project(":runtime"))
}