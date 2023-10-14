plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm()
    // other needed native platforms

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                api(project(":example:api:center"))
                api(libs.ktor.server.core)
            }
        }
    }
}