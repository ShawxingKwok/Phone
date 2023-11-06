plugins {
    val kt = "1.9.0"
    kotlin("multiplatform") version kt
    id ("com.google.devtools.ksp") version "$kt-1.0.13"
    kotlin("plugin.serialization") version kt
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
                // implementation("io.github.shawxingkwok:phone-runtime:1.0.0-SNAPSHOT")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
    }
}

dependencies {
    // other platforms are needless here, because this compiler generates code and copy them to else where.
    add("kspJvm", project(":compiler"))
    // add("kspJvm", "io.github.shawxingkwok:phone-compiler:1.0.0-1.0.0-SNAPSHOT")
}

ksp{
    // set your own local path
    arg("phone.server-package-path", "${projectDir.parent}/serverside/src/commonMain/kotlin")
    arg("phone.client-package-path", "${projectDir.parent}/clientside/src/commonMain/kotlin")

    arg("phone.server-package-name", "pers.shawxingkwok.server.phone")
    arg("phone.client-package-name", "pers.shawxingkwok.client.phone")

    arg("phone.default-method", "post")

    // optional
    // arg("phone.jwt-auth-name", "<your jwt auth name>")
    // arg("phone.client-phone-modifiers", "internal abstract") // open by default
}