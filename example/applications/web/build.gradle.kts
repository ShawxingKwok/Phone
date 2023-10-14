plugins {
    alias(libs.plugins.kt.multiplatform)
}

kotlin {
    js {
        binaries.executable()
        browser {
        }
    }
    sourceSets{
        val jsMain by getting {
            dependencies {
                // implementation(kotlin("stdlib-js"))
                implementation(project(":example:api:clientside"))
                implementation(libs.ktor.client.js)
            }
        }
    }
}

tasks.register<Copy>("moveJsLibs") {
    val rootPath = projectDir.path.substringBeforeLast("/example/applications/web")
    from("$rootPath/build/js")
    into("${buildDir.path}/js")
}

tasks.named("build") {
    finalizedBy("moveJsLibs")
}