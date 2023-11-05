package pers.shawxingkwok.center.serializers

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.phone.Phone

@Phone.Serializer
object TimeArrayOutSerializer : KSerializer<Array<out Time?>>{
    // Or convert to Array<IntArray?> directly in this case
    @Serializable
    private class Surrogate(val value: Array<out @Serializable(TimeSerializer::class) Time?>)

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("TimeArrayOut")

    override fun deserialize(decoder: Decoder): Array<out Time?> {
        return decoder.decodeSerializableValue(Surrogate.serializer()).value
    }

    override fun serialize(encoder: Encoder, value: Array<out Time?>) {
        encoder.encodeSerializableValue(Surrogate.serializer(), Surrogate(value))
    }
}