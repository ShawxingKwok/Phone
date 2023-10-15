import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kt.jvm)
    alias(libs.plugins.publish)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    // implementation(libs.shawxing.kspUtil)
    implementation(libs.shawxing.kspUtil)
    ksp(libs.shawxing.kspUtil)
    implementation(project(":runtime"))
    implementation(libs.ksp)
    implementation(libs.shawxing.ktUtil)
}

tasks.withType<KotlinCompile>().configureEach{
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xcontext-receivers",
        "-opt-in=com.google.devtools.ksp.KspExperimental",
    )
}

mavenPublishing {
    val isSnapshot = true
    val version = "1.0.0-1.0.0"
    coordinates("io.github.shawxingkwok", "phone-compiler", if (isSnapshot) "$version-SNAPSHOT" else version)
    pom {
        val repo = "Phone"
        name.set(repo)
        description.set("Phone compiler")
        inceptionYear.set("2023")

        url.set("https://github.com/ShawxingKwok/$repo/")

        scm{
            connection.set("scm:git:git://github.com/ShawxingKwok/$repo.git")
            developerConnection.set("scm:git:ssh://git@github.com/ShawxingKwok/$repo.git")
        }
    }
}