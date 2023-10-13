plugins {
    alias(libs.plugins.kt.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm()
    js()
    // other needed native platforms
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
    arg("phone.basic-url", "http://192.168.0.105:8080")
    // set your own path
    arg("phone.server-package-path", "/Users/william/IdeaProjects/library/Phone/example/api/serverside/src/commonMain/kotlin")
    arg("phone.client-package-path", "/Users/william/IdeaProjects/library/Phone/example/api/clientside/src/commonMain/kotlin")

    arg("phone.server-package-name", "pers.shawxingkwok.server.phone")
    arg("phone.client-package-name", "pers.shawxingkwok.client.phone")
}