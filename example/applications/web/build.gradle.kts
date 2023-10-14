plugins {
    kotlin("multiplatform") version "1.9.0"
    application
}

group = "pers.shawxingkwok.phone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
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
                implementation("io.ktor:ktor-server-netty:2.3.5")
                implementation("io.ktor:ktor-server-html-builder-jvm:2.3.5")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.1")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.346")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.9.3-pre.346")
                implementation(project(":example:api:clientside"))
                implementation(libs.ktor.client.js)
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

tasks.register<Copy>("moveJsLibs") {
    val rootPath = projectDir.path.substringBeforeLast("/example/applications/web")
    from("$rootPath/build/js")
    into("${buildDir.path}/js")
}

tasks.named("build") {
    finalizedBy("moveJsLibs")
}