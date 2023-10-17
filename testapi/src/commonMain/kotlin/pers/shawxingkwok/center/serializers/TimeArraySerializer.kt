package pers.shawxingkwok.center.serializers

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.phone.Phone

@Phone.Serializer
object TimeArraySerializer : KSerializer<Array<Time?>>{
    @Serializable
    private class Container(val value: Array<@Serializable(TimeSerializer::class) Time?>)

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("TimeArrContainer")

    override fun deserialize(decoder: Decoder): Array<Time?> {
        return decoder.decodeSerializableValue(Container.serializer()).value
    }

    override fun serialize(encoder: Encoder, value: Array<Time?>) {
        encoder.encodeSerializableValue(Container.serializer(), Container(value))
    }
}