plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm()
    // other needed native platforms could also be added
    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                implementation(project(":example:api:center"))
                implementation(libs.ktor.server.core)
                implementation(libs.serialization)
                implementation(libs.coroutines.core)
            }
        }
    }
}