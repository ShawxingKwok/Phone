package pers.shawxingkwok.phone

internal fun getCoderFunctions(): String =
    """
    private inline fun <reified T: Any> encode(
        value: T,
        serializer: KSerializer<T>?,
    ): String =
        ~when{
            value is String -> value
            serializer == null -> Json.encodeToString(value)
            else -> Json.encodeToString(serializer, value)
        }!~

    private inline fun <reified T: Any> decode(
        text: String,
        serializer: KSerializer<T>?
    ): T =
        ~when{
            T::class == String::class -> text as T
            serializer == null -> Json.decodeFromString(text)
            else -> Json.decodeFromString(serializer, text)
        }!~
    """.trim()