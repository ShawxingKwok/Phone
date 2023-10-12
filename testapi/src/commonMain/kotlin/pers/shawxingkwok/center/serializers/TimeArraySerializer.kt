package pers.shawxingkwok.center.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.Time

@Phone.Serializer
object TimeArraySerializer : KSerializer<Array<out Time>> {
    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): Array<out Time> {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Array<out Time>) {
        TODO("Not yet implemented")
    }
}