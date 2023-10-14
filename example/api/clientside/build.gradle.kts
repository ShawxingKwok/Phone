plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm()
    js{
        binaries.executable()
        browser()
    }
    // other needed native platforms

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                api(project(":example:api:center"))
                api(libs.ktor.client.core)
            }
        }
    }
}