plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.server.core)
                implementation(project(":testapi"))
            }
        }
    }
}