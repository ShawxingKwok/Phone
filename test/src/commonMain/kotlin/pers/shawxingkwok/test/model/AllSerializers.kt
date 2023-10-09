package pers.shawxingkwok.phonesample.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pers.shawxingkwok.phone.*
import kotlin.reflect.KClass

@Phone.KSerializers
val AllSerializers: Map<KClass<out Any>, KSerializer<out Any>> = mapOf(
    User::class to UserSerializer,
)

private object UserSerializer : KSerializer<User>{
    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): User {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: User) {
        TODO("Not yet implemented")
    }
}







