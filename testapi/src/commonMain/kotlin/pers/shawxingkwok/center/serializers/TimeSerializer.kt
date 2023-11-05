package pers.shawxingkwok.center.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.*
import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.Time

@Phone.Serializer
object TimeSerializer : KSerializer<Time> {
    @Serializable
    @SerialName("Time")
    private class Surrogate(val hour: Int, val min: Int, val sec: Int)

    override val descriptor: SerialDescriptor get() = Surrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Time {
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        return Time(surrogate.hour, surrogate.min, surrogate.sec)
    }

    override fun serialize(encoder: Encoder, value: Time) {
        val surrogate = Surrogate(value.hour, value.min, value.sec)
        encoder.encodeSerializableValue(Surrogate.serializer(), surrogate)
    }
}