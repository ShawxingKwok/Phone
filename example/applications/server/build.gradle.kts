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
    mainClass.set("server.mainKt")
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.server.core)
    implementation(project(":example:api:serverside"))
    implementation(libs.ktor.server.core)
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-websockets:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-websockets-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-cors-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")
    implementation("io.github.shawxingkwok:kt-util:1.0.2")
    implementation("io.ktor:ktor-server-partial-content-jvm:2.3.5")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:2.3.5")
    implementation(libs.ktor.server.netty)
    implementation("io.ktor:ktor-server-html-builder-jvm:${libs.versions.ktor.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    testImplementation ("io.ktor:ktor-server-tests-jvm:${libs.versions.ktor.get()}")
    testImplementation(libs.ktor.client.core)
    testImplementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-auth:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
}