pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        jcenter()
        mavenCentral()

        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://www.jitpack.io")
        maven("https://nodejs.org/dist")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }
}

// plugins {
//     id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
// }

rootProject.name = "Phone"

include("runtime")
include("compiler")
include("testapi")
include("testcaller")
include("example:api:shared")
include("example:api:clientside")
include("example:api:serverside")
include("example:applications:android")
include("example:applications:web")