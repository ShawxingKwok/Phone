pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://www.jitpack.io")
    }
}

// plugins {
//     id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
// }

rootProject.name = "Phone"

include(":runtime")
include(":compiler")
include("testapi")
include("testcaller")
