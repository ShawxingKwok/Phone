plugins {
    alias(libs.plugins.ktMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm {
        jvmToolchain(8)
        withJava()
    }

    sourceSets {
        val commonMain by getting{
            dependencies{
                implementation(project(":runtime"))
                implementation(libs.serialization)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.server.core)
            }
        }
    }
}

dependencies {
    // add("kspCommonMainMetadata", project(":compiler"))
    add("kspJvm", project(":compiler"))
}

ksp{
    arg("ksp-util.debug", "")
    arg("phone.server-package-path", "/Users/william/IdeaProjects/library/Phone/testcaller/src/commonMain/kotlin")
    arg("phone.client-package-path", "/Users/william/IdeaProjects/library/Phone/testcaller/src/commonMain/kotlin")
    arg("phone.server-package-name", "pers.shawxingkwok.test")
    arg("phone.client-package-name", "pers.shawxingkwok.test")
}