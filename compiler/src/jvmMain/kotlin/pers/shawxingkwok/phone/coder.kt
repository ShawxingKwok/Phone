@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone

import com.google.devtools.ksp.getClassDeclarationByName
import pers.shawxingkwok.ksputil.KtGen
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.text
import kotlin.reflect.KClass

internal fun KtGen.coder(): String {
    return if (MyProcessor.allSerializersProp == null)
        """
        private fun encode(value: Any): String =
            ~if (value is String) value
            else Json.encodeToString(value)!~
            
        private inline fun <reified T> decode(text: String): T =
            ~if(T::class == String::class) text as T
            else Json.decodeFromString(text)!~
        """.trim()
    else{
        val _AllSerializers = MyProcessor.allSerializersProp.text
        val _SerializationStrategy = resolver.getClassDeclarationByName("kotlinx.serialization.SerializationStrategy")!!.text
        val _DeserializationStrategy = resolver.getClassDeclarationByName("kotlinx.serialization.DeserializationStrategy")!!.text
        val _KClass = resolver.getClassDeclarationByName(KClass::class.qualifiedName!!)!!.text
        val _KSerializer = resolver.getClassDeclarationByName("kotlinx.serialization.KSerializer")!!.text

        """
        @Suppress("UNCHECKED_CAST")
        private fun encode(value: Any): String {
            if (value is String) return value
    
            return when(val customSerializer = $_AllSerializers[value::class] as $_SerializationStrategy<Any>?){
                null -> Json.encodeToString(value)
                else -> Json.encodeToString(customSerializer, value)
            }
        }
    
        @Suppress("UNCHECKED_CAST")
        private inline fun <reified T> decode(text: String): T {
            if (T::class == String::class)
                ~return text as T!~
    
            $_AllSerializers as Map<$_KClass<out Any>, $_KSerializer<Any>>
    
            return when(val customSerializer = $_AllSerializers[T::class] as $_DeserializationStrategy<T>?){
                null -> Json.decodeFromString(text)
                else -> Json.decodeFromString(customSerializer, text)
            }
        }
        """.trimIndent()
    }
}