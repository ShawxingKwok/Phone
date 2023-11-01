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
        browser{
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.websockets)
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.9.1")
                implementation(project(":example:api:clientside"))

                //React, React DOM + Wrappers (chapter 3)
                implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.636"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy")

                //Kotlin React Emotion (CSS) (chapter 3)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

                //Video Player (chapter 7)
                implementation(npm("react-player", "2.12.0"))

                //Share Buttons (chapter 7)
                implementation(npm("react-share", "4.4.1"))

                //Coroutines & serialization (chapter 8)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
    }
}

tasks.register("stage") {
    dependsOn("build")
}