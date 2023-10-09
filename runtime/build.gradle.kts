import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.ktMultiplatform)
    alias(libs.plugins.publish)
}

kotlin {
    explicitApi()

    jvm {
        jvmToolchain(8)
        withJava()
    }
    js {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    macosX64()
    macosArm64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()
    watchosX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    mingwX64()

    linuxX64()

    sourceSets {
        val commonMain by getting
    }
}

mavenPublishing {
    val isSnapshot = false
    val version = "1.0.0"
    coordinates("io.github.shawxingkwok", "phone-runtime", if (isSnapshot) "$version-SNAPSHOT" else version)
    pom {
        val repo = "Phone"
        name.set(repo)
        description.set("Phone sublibrary runtime")
        inceptionYear.set("2023")

        url.set("https://github.com/ShawxingKwok/$repo/")

        scm{
            connection.set("scm:git:git://github.com/ShawxingKwok/$repo.git")
            developerConnection.set("scm:git:ssh://git@github.com/ShawxingKwok/$repo.git")
        }
    }
}

rootProject.plugins.withType(YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = false // true
}