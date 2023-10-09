import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.ktMultiplatform)
    alias(libs.plugins.publish)
    alias(libs.plugins.ksp)
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
    sourceSets {
        val jvmMain by getting{
            dependencies {
                implementation(project(":runtime"))
                implementation(libs.ksp)
                implementation(libs.shawxing.kspUtil)
                implementation(libs.shawxing.ktUtil)
            }
        }
    }
}

dependencies {
    add("kspJvm", libs.shawxing.kspUtil)
}

tasks.withType<KotlinCompile>().configureEach{
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}

mavenPublishing {
    val isSnapshot = false
    val version = "1.0.0"
    coordinates("io.github.shawxingkwok", "phone-compiler", if (isSnapshot) "$version-SNAPSHOT" else version)
    pom {
        val repo = "Phone"
        name.set(repo)
        description.set("Phone sublibrary compiler")
        inceptionYear.set("2023")

        url.set("https://github.com/ShawxingKwok/$repo/")

        scm{
            connection.set("scm:git:git://github.com/ShawxingKwok/$repo.git")
            developerConnection.set("scm:git:ssh://git@github.com/ShawxingKwok/$repo.git")
        }
    }
}