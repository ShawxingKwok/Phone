package pers.shawxingkwok.test.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import pers.shawxingkwok.phone.Phone

// Suppose `class Time` is from a third-party library
// and is not adapted with Kotlin Serializable
class Time(val hour: Int, val min: Int, val sec: Int)