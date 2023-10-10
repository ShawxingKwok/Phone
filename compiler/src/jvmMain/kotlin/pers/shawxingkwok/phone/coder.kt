package pers.shawxingkwok.phone

internal fun getCoderFunctions(): String =
    """
    @Suppress("UNCHECKED_CAST")
    private fun encode(value: Any, serializer: KSerializer<out Any>?): String =
        ~when{
            value is String -> value
            serializer == null -> Json.encodeToString(value)
            else -> Json.encodeToString(serializer as SerializationStrategy<Any>, value)
        }!~

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> decode(
        text: String,
        serializer: KSerializer<out Any>?
    ): T = 
        ~when{
            T::class == String::class -> text as T
            serializer == null -> Json.decodeFromString(text)
            else -> Json.decodeFromString(serializer as DeserializationStrategy<T>, text)
        }!~
    """.trim()