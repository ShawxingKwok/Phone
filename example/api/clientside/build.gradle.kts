plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm()
    js()
    // other needed native platforms

    sourceSets {
        val commonMain by getting{
            dependencies{
                api(project(":example:api:center"))
                api(libs.ktor.client.core)
            }
        }
    }
}