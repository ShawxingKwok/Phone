plugins {
    alias(libs.plugins.kt.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
}

kotlin {
    jvmToolchain(8)
}

application{
    mainClass.set("io.ktor.server.netty.EngineMain")
}

// val ktor = libs.versions.ktor.get()

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.serialization.json)
    implementation(libs.serialization.core)
    implementation(libs.shawxing.ktUtil)
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation(project(":testapi"))
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-websockets-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-partial-content-jvm")
    implementation("io.ktor:ktor-server-auto-head-response-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-content-negotiation")

    testImplementation ("io.ktor:ktor-server-tests-jvm")
    testImplementation("io.ktor:ktor-server-test-host")

    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation(libs.ktor.client.core)
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-auth")
    testImplementation("io.ktor:ktor-client-websockets")
}