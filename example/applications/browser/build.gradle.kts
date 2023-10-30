plugins {
    alias(libs.plugins.kt.multiplatform)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
    application
}

application {
    mainClass.set("mainKt")
}

kotlin {
    js {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.websockets)
                implementation(project(":example:api:clientside"))
            }
        }
    }
}

tasks.register("stage") {
    dependsOn("build")
}