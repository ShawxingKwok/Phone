package pers.shawxingkwok.test.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import pers.shawxingkwok.phone.Phone

// Suppose `class X` is from a third-party library
// and is not the subclass of Serializable
class Time(val hour: Int, val min: Int, val sec: Int)

@Phone.Serializer
object TimeSerializer : KSerializer<Time> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Time") {
        this.element<Int>("hour")
        this.element<Int>("min")
        this.element<Int>("sec")
    }

    override fun deserialize(decoder: Decoder): Time =
        decoder.decodeStructure(descriptor) {
            var hour = -1
            var min = -1
            var sec = -1

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> hour = decodeIntElement(descriptor, 0)
                    1 -> min = decodeIntElement(descriptor, 1)
                    2 -> sec = decodeIntElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            Time(hour, min, sec)
        }

    override fun serialize(encoder: Encoder, value: Time) {
        encoder.encodeStructure(descriptor){
            encodeIntElement(descriptor, 0, value.hour)
            encodeIntElement(descriptor, 1, value.min)
            encodeIntElement(descriptor, 2, value.sec)
        }
    }
}

fun main() {
    val encoded = Json.encodeToString(TimeSerializer, Time(0, 1, 2))
    Json.decodeFromString(TimeSerializer, encoded).hour.let(::println)
}