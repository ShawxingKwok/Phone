plugins {
    alias(libs.plugins.kt.jvm)
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

    testImplementation(libs.ktor.client.core)
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-auth:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
}