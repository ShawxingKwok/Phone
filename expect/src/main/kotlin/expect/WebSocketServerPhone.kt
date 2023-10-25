/*
package expect

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pers.shawxingkwok.phone.Phone

typealias WebSocketConnector = suspend DefaultWebSocketServerSession.() -> Unit
typealias WebSocketRawConnector = suspend WebSocketServerSession.() -> Unit
typealias CommonConnector<T> = suspend PipelineContext<Unit, ApplicationCall>.() -> T

object WebSocketServerPhone{
    interface TestApi : expect.TestApi {
        fun Route.doOtherTasks(){}

        override suspend fun get(i: Int): WebSocketConnector

        override suspend fun obtain(i: Int): WebSocketRawConnector

        override suspend fun search(id: String): CommonConnector<User?>
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

    // insert if any websocket
    private suspend fun WebSocketServerSession.unacceptedClose(text: String){
        val closeReason = CloseReason(CloseReason.Codes.CANNOT_ACCEPT, text)
        close(closeReason)
    }

    @JvmName("TestApi")
    fun route(
        route: Route,
        testApi: TestApi,
    ){
        route.route("/TestApi") {
            testApi.run { doOtherTasks() }

            post("/search"){
                val params = call.request.queryParameters

                val ret = testApi.search(
                    id = params["id"]
                        ?.let {
                            try {
                                decode<String>(it, null, null)
                            }catch (tr: Throwable){
                                call.respondText(
                                    text = "The parameter `id` is incorrectly serialized.\n$tr",
                                    status = HttpStatusCode.BadRequest
                                )
                                return@post
                            }
                        }
                        // if non-nullable
                        ?: return@post call.respondText(
                            text = "Not found `id` in parameters.",
                            status = HttpStatusCode.BadRequest
                        )
                )
                .invoke(this)

                // if nullable
                ret ?: return@post call.response.status(HttpStatusCode.NotFound)

                val text = encode(ret, User.serializer(), null)
                call.respondText(text, status = HttpStatusCode.OK)
            }

            // may be raw
            webSocket("/get") {
                val params = call.request.queryParameters

                testApi.get(
                    i = params["i"]
                        ?.let {
                            try {
                                decode<Int>(it, null, null)
                            }catch (tr: Throwable){
                                unacceptedClose("The parameter `id` is incorrectly serialized.\n$tr")
                                return@webSocket
                            }
                        }
                        ?: return@webSocket unacceptedClose("Not found `i` in parameters.")
                )
                .invoke(this)
            }
        }
    }
}
*/
