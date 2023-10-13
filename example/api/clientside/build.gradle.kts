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
                api(project(":example:api:center"))
                implementation(libs.ktor.client.core)
            }
        }
    }
}