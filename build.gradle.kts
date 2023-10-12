plugins {
    alias(libs.plugins.kt.multiplatform) apply false
    alias(libs.plugins.kt.jvm) apply false
    alias(libs.plugins.kt.android) apply false
    alias(libs.plugins.android) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.ktor) apply false
}