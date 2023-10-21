plugins {
    alias(libs.plugins.kt.multiplatform)
    alias(libs.plugins.publish)
}

val isSnapshot = true

kotlin {
    explicitApiWarning()

    jvm{
        jvmToolchain(8)
    }

    js{
        browser()
    }

    sourceSets {
        val commonMain by getting
    }

    if (isSnapshot) return@kotlin

    @Suppress("OPT_IN_USAGE")
    wasm {
        browser()
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
}

mavenPublishing {
    val version = "1.0.0"
    coordinates("io.github.shawxingkwok", "phone-runtime", if (isSnapshot) "$version-SNAPSHOT" else version)
    pom {
        val repo = "Phone"
        name.set(repo)
        description.set("Phone runtime library")
        inceptionYear.set("2023")

        url.set("https://github.com/ShawxingKwok/$repo/")

        scm{
            connection.set("scm:git:git://github.com/ShawxingKwok/$repo.git")
            developerConnection.set("scm:git:ssh://git@github.com/ShawxingKwok/$repo.git")
        }
    }
}