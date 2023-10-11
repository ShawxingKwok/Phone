plugins {
    alias(libs.plugins.ktMultiplatform)
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