@file:Suppress("LocalVariableName", "DuplicatedCode", "SameParameterValue")

package pers.shawxingkwok.test.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.DeserializationStrategy
import pers.shawxingkwok.test.model.TimeSerializer
import pers.shawxingkwok.test.model.TimeArraySerializer

object Phone{
    abstract class AccountApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.AccountApi
    abstract class ChatApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.ChatApi
    abstract class TimeApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.TimeApi

    @Suppress("UNCHECKED_CAST")
    private fun encode(value: Any, serializer: KSerializer<out Any>?): String =
        when{
            value is String -> value
            serializer == null -> Json.encodeToString(value)
            else -> Json.encodeToString(serializer as SerializationStrategy<Any>, value)
        }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> decode(
        text: String,
        serializer: KSerializer<out Any>?
    ): T =
        when{
            T::class == String::class -> text as T
            serializer == null -> Json.decodeFromString(text)
            else -> Json.decodeFromString(serializer as DeserializationStrategy<T>, text)
        }

    fun configure(
        routing: Routing,
        getAccountApi: (ApplicationCall) -> AccountApi,
        getChatApi: (ApplicationCall) -> ChatApi,
        getTimeApi: (ApplicationCall) -> TimeApi,
    ){
        routing.route("AccountApi"){
            get("/login"){
                val params = call.request.queryParameters

                val ret = getAccountApi(call).login(
                    email = params["email"]
                        ?: return@get call.respondText(
                            text = "Not found `email` in parameters.",
                            status = HttpStatusCode.BadRequest,
                        ),
                    password = params["password"]
                        ?: return@get call.respondText(
                            text = "Not found `password` in parameters.",
                            status = HttpStatusCode.BadRequest,
                        ),
                    verificationCode = params["verificationCode"],
                )
                val text = encode(ret, null)
                call.respondText(text, status = HttpStatusCode.OK)
            }

            get("/delete"){
                val params = call.request.queryParameters

                getAccountApi(call).delete(
                    id = params["id"]?.let{
                            try {
                                decode(it, null)
                            }catch (_: Throwable){
                                val text = "The parameter id is incorrectly serialized."
                                call.respondText(text, status = HttpStatusCode.BadRequest)
                                return@get
                            }
                        }
                        ?: return@get call.respondText(
                            text = "Not found `id` in parameters.",
                            status = HttpStatusCode.BadRequest,
                        ),
                )
                call.response.status(HttpStatusCode.OK)
            }

            get("/search"){
                val params = call.request.queryParameters

                val ret = getAccountApi(call).search(
                    id = params["id"]?.let{
                            try {
                                decode(it, null)
                            }catch (_: Throwable){
                                val text = "The parameter id is incorrectly serialized."
                                call.respondText(text, status = HttpStatusCode.BadRequest)
                                return@get
                            }
                        }
                        ?: return@get call.respondText(
                            text = "Not found `id` in parameters.",
                            status = HttpStatusCode.BadRequest,
                        ),
                )
                if(ret == null)
                    call.response.status(HttpStatusCode.NotFound)
                else{
                    val text = encode(ret, null)
                    call.respondText(text, status = HttpStatusCode.OK)
                }
            }
        }

        routing.route("ChatApi"){
            get("/getChats"){
                val ret = getChatApi(call).getChats()
                val text = encode(ret, null)
                call.respondText(text, status = HttpStatusCode.OK)
            }
        }

        routing.route("TimeApi"){
            get("/getTime"){
                val ret = getTimeApi(call).getTime()
                val text = encode(ret, TimeSerializer)
                call.respondText(text, status = HttpStatusCode.OK)
            }

            get("/sumTime"){
                val params = call.request.queryParameters

                val ret = getTimeApi(call).sumTime(
                    times = params["times"]?.let{
                            try {
                                decode(it, TimeArraySerializer)
                            }catch (_: Throwable){
                                val text = "The parameter times is incorrectly serialized."
                                call.respondText(text, status = HttpStatusCode.BadRequest)
                                return@get
                            }
                        }
                        ?: return@get call.respondText(
                            text = "Not found `times` in parameters.",
                            status = HttpStatusCode.BadRequest,
                        ),
                )
                val text = encode(ret, TimeSerializer)
                call.respondText(text, status = HttpStatusCode.OK)
            }
        }
    }
}