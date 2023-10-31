plugins {
    alias(libs.plugins.kt.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
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
                api(libs.serialization.json)
                // help custom serializers
                implementation(libs.serialization.core)
            }
        }
    }
}

dependencies {
    add("kspJvm", project(":compiler"))
}

ksp{
    arg("phone.default-method", "post")

    // you could set your own local path
    arg("phone.server-package-path", "${projectDir.parent}/serverside/src/commonMain/kotlin")
    arg("phone.client-package-path", "${projectDir.parent}/clientside/src/commonMain/kotlin")

    arg("phone.server-package-name", "pers.shawxingkwok.server.phone")
    arg("phone.client-package-name", "pers.shawxingkwok.client.phone")
}