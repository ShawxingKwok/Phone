package pers.shawxingkwok.phone

internal fun getCoderFunctions(): String =
    """
    @Suppress("UNCHECKED_CAST")
    private fun encode(value: Any, serializer: KSerializer<out Any>?): String{
        serializer as KSerializer<Any>?

        return when{
            value is String -> value
            serializer == null -> Json.encodeToString(value)
            else -> Json.encodeToString(serializer, value)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> decode(
        text: String, 
        serializer: KSerializer<out Any>?
    ): T {
        serializer as KSerializer<Any>?
    
        return when{
            T::class == String::class -> text 
            serializer == null -> Json.decodeFromString(text)
            else -> Json.decodeFromString(serializer, text) 
        } as T
    }
    """.trim()