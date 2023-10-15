package pers.shawxingkwok.phone

internal fun getCoderFunctions(): String =
    """
    private inline fun <reified T: Any> encode(
        value: T,
        serializer: KSerializer<T>?,
        cipher: Phone.Cipher?,
    ): String =
        ~when(value){
            is String -> value
            is Boolean, is Int, is Long,
            is Float, is Double 
                ~-> value.toString()!~

            else -> {
                if (serializer == null)
                    Json.encodeToString(value)
                else
                    Json.encodeToString(serializer, value)
            }
        }
        .let { text ->
            if (cipher == null) text
            else {
                val utfBytes = text.encodeToByteArray()
                val base64Bytes = cipher.encrypt(utfBytes)
                Json.encodeToString(ByteArraySerializer(), base64Bytes)
            }
        }!~

    private inline fun <reified T: Any> decode(
        text: String,
        serializer: KSerializer<T>?,
        cipher: Phone.Cipher?,
    ): T {
        var newText = text
        if (cipher != null) {
            val base64Bytes = Json.decodeFromString(ByteArraySerializer(), text)
            val utf8Bytes = cipher.decrypt(base64Bytes)
            newText = utf8Bytes.decodeToString()
        }

        return when{
            T::class == String::class -> newText 
            T::class == Boolean::class -> newText.toBoolean() 
            T::class == Int::class -> newText.toInt() 
            T::class == Long::class -> newText.toLong() 
            T::class == Float::class -> newText.toFloat() 
            T::class == Double::class -> newText.toDouble() 
            serializer == null -> Json.decodeFromString(newText)
            else -> Json.decodeFromString(serializer, newText)
        } as T
    }
    """.trim()