package pers.shawxingkwok.phone

internal fun getCoderFunctions(): String =
    """
	private inline fun <reified T> encode(
		value: T,
		serializer: KSerializer<T & Any>?,
		cipher: Phone.Cipher?,
	): String =
		~when(value){
			null -> "null"
			
			is String -> value

			is Boolean, is Int, is Long,
			is Float, is Double
				~-> value.toString()!~

			else ->
				~if (serializer == null)
					~Json.encodeToString<T>(value)!~
				else
					~Json.encodeToString(serializer, value)!~!~
		}
		.let { text ->
			if (cipher == null) text
			else {
				val utf8Bytes = text.encodeToByteArray()
				val encrypted = cipher.encrypt(utf8Bytes)
				Json.encodeToString(ByteArraySerializer(), encrypted)
			}
		}!~

	@Suppress("NAME_SHADOWING")
	private inline fun <reified T> decode(
		text: String,
		serializer: KSerializer<T & Any>?,
		cipher: Phone.Cipher?,
	): T {
		var text = text
		if (cipher != null) {
			val encrypted = Json.decodeFromString(ByteArraySerializer(), text)
			val utf8Bytes = cipher.decrypt(encrypted)
			text = utf8Bytes.decodeToString()
		}

		return when{
			text == "null" -> null as T			
			serializer != null -> Json.decodeFromString(serializer, text)
			T::class == String::class -> text as T
			T::class == Boolean::class -> text.toBoolean() as T
			T::class == Int::class -> text.toInt() as T
			T::class == Long::class -> text.toLong() as T
			T::class == Float::class -> text.toFloat() as T
			T::class == Double::class -> text.toDouble() as T
			// `T` must be put here, or `Any` would be used for searching the serializer.
			else -> Json.decodeFromString(text) as T
		}
	}
    """