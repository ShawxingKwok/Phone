package expect

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pers.shawxingkwok.phone.Phone
import kotlin.reflect.KClass

open class WebSocketClientPhone(
    val client: HttpClient,
    private val basicUrl: String = "http://localhost:80",
    private val tokenScheme: String = "Bearer",
    var token: String? = null,
) {
    init{
        check(
            basicUrl.startsWith("http://")
            || basicUrl.startsWith("https://")
        )
    }

    protected open fun HttpRequestBuilder.onEachRequest(apiKClass: KClass<*>) {}

    private fun HttpRequestBuilder.addToken() {
        checkNotNull(token){
            "Set token before the request with authentication."
        }
        header(HttpHeaders.Authorization, "$tokenScheme $token")
    }

    private inline fun <reified T> addParamWithJson(
        add: (String, String) -> Unit,
        key: String,
        value: T,
        serializer: KSerializer<T & Any>?,
        cipher: Phone.Cipher?,
    ){
        if (value == null) return
        val newV = encode(value, serializer, cipher)
        add(key, newV)
    }

    private suspend fun HttpResponse.checkIsOK() {
        check(status == HttpStatusCode.OK){
            bodyAsText()
        }
    }

    private inline fun <reified T: Any> encode(
        value: T,
        serializer: KSerializer<T>?,
        cipher: Phone.Cipher?,
    ): String =
        when(value){
            is String -> value

            is Boolean, is Int, is Long,
            is Float, is Double
            -> value.toString()

            else ->
                if (serializer == null)
                    Json.encodeToString(value)
                else
                    Json.encodeToString(serializer, value)
        }
        .let { text ->
            if (cipher == null) text
            else {
                val utfBytes = text.encodeToByteArray()
                val base64Bytes = cipher.encrypt(utfBytes)
                Json.encodeToString(ByteArraySerializer(), base64Bytes)
            }
        }

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
            serializer != null -> Json.decodeFromString(serializer, newText)
            T::class == String::class -> newText as T
            T::class == Boolean::class -> newText.toBoolean() as T
            T::class == Int::class -> newText.toInt() as T
            T::class == Long::class -> newText.toLong() as T
            T::class == Float::class -> newText.toFloat() as T
            T::class == Double::class -> newText.toDouble() as T
            // `T` must be put here, or `Any` would be used for searching the serializer.
            else -> Json.decodeFromString(newText) as T
        }
    }

    inner class TestApi(
        private val extendRequest: (HttpRequestBuilder.() -> Unit)? = null,
    )
        : expect.TestApi
    {
        override suspend fun search(id: String): Result<User?> =
            runCatching{
                val response = client.submitForm(
                    url = "http://localhost:80/TestApi/delete",
                    formParameters = parameters{
                        addParamWithJson(::append,"id", id, null, null)
                    },
                    encodeInQuery = false,
                ){
                    onEachRequest(this@TestApi::class)
                    extendRequest?.invoke(this)
                }

                if(response.status == HttpStatusCode.NotFound)
                    return@runCatching null

                response.checkIsOK()

                decode(response.bodyAsText(), User.serializer(), null)
            }

        override suspend fun delete(id: String): Result<Unit> =
            runCatching{
                val response = client.submitForm(
                    url = "http://localhost:80/TestApi/delete",
                    formParameters = parameters{
                        addParamWithJson(::append,"id", id, null, null)
                    },
                    encodeInQuery = false,
                ){
                    onEachRequest(this@TestApi::class)
                    extendRequest?.invoke(this)
                }

                response.checkIsOK()
            }

        override suspend fun get(i: Int): Result<DefaultClientWebSocketSession> =
            runCatching {
                client.webSocketSession(
                    path = "TestApi/get",
                    host = "localhost", port = 80,
                    block = {
                        onEachRequest(this@TestApi::class)
                        extendRequest?.invoke(this)
                        addParamWithJson(::parameter,"i", i, null, null)
                    },
                )
            }

        override suspend fun obtain(i: Int): Result<ClientWebSocketSession> =
            runCatching {
                client.webSocketRawSession(
                    path = "TestApi/obtain",
                    host = "localhost", port = 80,
                    block = {
                        onEachRequest(this@TestApi::class)
                        extendRequest?.invoke(this)
                        addParamWithJson(::parameter,"i", i, null, null)
                    },
                )
            }
    }
}
