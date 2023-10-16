plugins {
    alias(libs.plugins.kt.multiplatform)
    application
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":example:api:serverside"))
                implementation(libs.ktor.server.netty)
                implementation("io.ktor:ktor-server-html-builder-jvm:${libs.versions.ktor.get()}")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(project(":example:api:clientside"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("pers.shawxingkwok.phone.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}
dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.5")
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.5")
}

// tasks.register<Copy>("moveJsLibs") {
//     val rootPath = projectDir.path.substringBeforeLast("/example/applications/web")
//     from("$rootPath/build/js")
//     into("${buildDir.path}/js")
// }

// tasks.named("build") {
//     finalizedBy("moveJsLibs")
// }