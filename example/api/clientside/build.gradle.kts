plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm()
    js()
    // other needed native platforms could also be added
    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                implementation(project(":example:api:center"))
                implementation(libs.ktor.client.core)
                implementation(libs.serialization)
            }
        }
    }
}