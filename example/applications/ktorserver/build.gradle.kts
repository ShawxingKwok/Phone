plugins {
    alias(libs.plugins.kt.jvm)
    alias(libs.plugins.ktor)
}

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    // testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${libs.versions.kt}")

    implementation(project(":example:api:serverside"))
}