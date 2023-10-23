package pers.shawxingkwok.center.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.Time

@Phone.Serializer
object TimeSerializer : KSerializer<Time> {
    @Serializable
    private class Converter(val hour: Int, val min: Int, val sec: Int)

    override val descriptor: SerialDescriptor get() = buildClassSerialDescriptor("Time")

    override fun deserialize(decoder: Decoder): Time {
        val converter = decoder.decodeSerializableValue(Converter.serializer())
        return Time(converter.hour, converter.min, converter.sec)
    }

    override fun serialize(encoder: Encoder, value: Time) {
        val converter = Converter(value.hour, value.min, value.sec)
        encoder.encodeSerializableValue(Converter.serializer(), converter)
    }
}