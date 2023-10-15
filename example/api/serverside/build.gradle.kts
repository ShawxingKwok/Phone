plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm{
        jvmToolchain(8)
    }
    // other needed native platforms

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                api(project(":example:api:shared"))
                api(libs.ktor.server.core)

                // optional
                implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-server-websockets:${libs.versions.ktor.get()}")
            }
        }
    }
}