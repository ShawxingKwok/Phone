import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

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
        browser {
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
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
                implementation(libs.ktor.client.core)
                implementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-auth:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-client-websockets:${libs.versions.ktor.get()}")
                implementation(project(":example:api:clientside"))
                implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.430"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")

                //Kotlin React Emotion (CSS) (chapter 3)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

                //Video Player (chapter 7)
                implementation(npm("react-player", "2.12.0"))

                //Share Buttons (chapter 7)
                implementation(npm("react-share", "4.4.1"))

                //Coroutines & serialization (chapter 8)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
            }
        }
    }
}

// tasks.withType<Kotlin2JsCompile> {
//     kotlinOptions.outputFile = "${project.buildDir.path}/js/${project.name}.js"
// }
// Heroku Deployment (chapter 9)
tasks.register("stage") {
    dependsOn("build")
}