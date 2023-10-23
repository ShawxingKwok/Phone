package pers.shawxingkwok.center.serializers

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.phone.Phone

@Phone.Serializer
object TimeArraySerializer : KSerializer<Array<Time?>>{
    // Or convert to Array<IntArray?> directly in this case
    @Serializable
    private class Converter(val value: Array<@Serializable(TimeSerializer::class) Time?>)

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("TimeArrContainer")

    override fun deserialize(decoder: Decoder): Array<Time?> {
        return decoder.decodeSerializableValue(Converter.serializer()).value
    }

    override fun serialize(encoder: Encoder, value: Array<Time?>) {
        encoder.encodeSerializableValue(Converter.serializer(), Converter(value))
    }
}