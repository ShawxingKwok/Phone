plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    jvm{
        jvmToolchain(8)
    }
    js{
        browser()
    }
    // other needed native platforms

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                api(project(":example:api:shared"))
                api(libs.ktor.client.core)
            }
        }
    }
}