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
    implementation(project(":example:api:serverside"))
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.websockets.jvm)
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}