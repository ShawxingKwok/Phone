plugins {
    alias(libs.plugins.kt.jvm)
    alias(libs.plugins.ktor)
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.server.core)
    implementation(project(":testapi"))
    implementation(libs.ktor.server.core)
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-websockets:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-websockets-jvm:${libs.versions.ktor.get()}")
    // implementation("ch.qos.logback:logback-classic:1.4.11")

    testImplementation ("io.ktor:ktor-server-tests-jvm:${libs.versions.ktor.get()}")
    testImplementation(libs.ktor.client.core)
    testImplementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-auth:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
}