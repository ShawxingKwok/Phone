plugins {
    alias(libs.plugins.kt.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting{
            dependencies{
                api(project(":runtime"))
                api(libs.serialization.json)
                // help custom serializers
                implementation(libs.serialization.core)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":compiler"))
    add("kspJvm", project(":compiler"))
}

ksp{
    arg("ksp-util.debug", "")
    // set your own path
    arg("phone.server-package-path", "${projectDir.parent}/testcaller/src/main/kotlin")
    arg("phone.client-package-path", "${projectDir.parent}/testcaller/src/test/kotlin")

    arg("phone.server-package-name", "pers.shawxingkwok.test.server")
    arg("phone.client-package-name", "pers.shawxingkwok.test.client")
}