package pers.shawxingkwok.center.model

import kotlinx.serialization.Serializable

// Suppose `class Time` is from a third-party library
// and is not adapted with Kotlin Serializable
data class Time(val hour: Int, val min: Int, val sec: Int)