plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
    }

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.server.core)
                implementation(libs.serialization)
                implementation(project(":testapi"))
                implementation(project(":runtime"))
            }
        }
    }
}