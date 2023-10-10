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
import kotlin.String
import kotlin.Long
import pers.shawxingkwok.test.model.Time
import kotlin.Int

object Phone{
    abstract class AccountApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.AccountApi
    abstract class ChatApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.ChatApi
    abstract class TimeApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.TimeApi
    abstract class VarargApi(protected val call: ApplicationCall) : pers.shawxingkwok.test.api.VarargApi

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
        getVarargApi: (ApplicationCall) -> VarargApi,
    ){
        routing.route("AccountApi"){
            get("/login"){
                val params = call.request.queryParameters

                val _email: String = params["email"]
                    ?: return@get call.respondText(
                        text = "Not found email in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                val _password: String = params["password"]
                    ?: return@get call.respondText(
                        text = "Not found password in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                val _verificationCode: String? = params["verificationCode"]

                val ret = getAccountApi(call).login(_email, _password, _verificationCode)
                val text = encode(ret, null)
                call.respondText(text, status = HttpStatusCode.OK)
            }

            get("/delete"){
                val params = call.request.queryParameters

                val _id: Long = params["id"]
                    ?.let{
                        try {
                            decode(it, null)
                        }catch (_: Throwable){
                            val text = "The id is incorrectly serialized."
                            call.respondText(text, status = HttpStatusCode.BadRequest)
                            return@get
                        }
                    }
                    ?: return@get call.respondText(
                        text = "Not found id in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                getAccountApi(call).delete(_id)
                call.response.status(HttpStatusCode.OK)
            }

            get("/search"){
                val params = call.request.queryParameters

                val _id: Long = params["id"]
                    ?.let{
                        try {
                            decode(it, null)
                        }catch (_: Throwable){
                            val text = "The id is incorrectly serialized."
                            call.respondText(text, status = HttpStatusCode.BadRequest)
                            return@get
                        }
                    }
                    ?: return@get call.respondText(
                        text = "Not found id in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                val ret = getAccountApi(call).search(_id)
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
                val text = encode(ret, null)
                call.respondText(text, status = HttpStatusCode.OK)
            }

            get("/sumTime"){
                val params = call.request.queryParameters

                val _a: Time = params["a"]
                    ?.let{
                        try {
                            decode(it, null)
                        }catch (_: Throwable){
                            val text = "The a is incorrectly serialized."
                            call.respondText(text, status = HttpStatusCode.BadRequest)
                            return@get
                        }
                    }
                    ?: return@get call.respondText(
                        text = "Not found a in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                val _b: Time = params["b"]
                    ?.let{
                        try {
                            decode(it, null)
                        }catch (_: Throwable){
                            val text = "The b is incorrectly serialized."
                            call.respondText(text, status = HttpStatusCode.BadRequest)
                            return@get
                        }
                    }
                    ?: return@get call.respondText(
                        text = "Not found b in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                val ret = getTimeApi(call).sumTime(_a, _b)
                val text = encode(ret, null)
                call.respondText(text, status = HttpStatusCode.OK)
            }
        }

        routing.route("VarargApi"){
            get("/foo"){
                val params = call.request.queryParameters

                val _ints: Int = params["ints"]
                    ?.let{
                        try {
                            decode(it, null)
                        }catch (_: Throwable){
                            val text = "The ints is incorrectly serialized."
                            call.respondText(text, status = HttpStatusCode.BadRequest)
                            return@get
                        }
                    }
                    ?: return@get call.respondText(
                        text = "Not found ints in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                getVarargApi(call).foo(_ints)
                call.response.status(HttpStatusCode.OK)
            }

            get("/bar"){
                val params = call.request.queryParameters

                val _str: String = params["str"]
                    ?: return@get call.respondText(
                        text = "Not found str in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                getVarargApi(call).bar(_str)
                call.response.status(HttpStatusCode.OK)
            }

            get("/baz"){
                val params = call.request.queryParameters

                val _times: Time = params["times"]
                    ?.let{
                        try {
                            decode(it, null)
                        }catch (_: Throwable){
                            val text = "The times is incorrectly serialized."
                            call.respondText(text, status = HttpStatusCode.BadRequest)
                            return@get
                        }
                    }
                    ?: return@get call.respondText(
                        text = "Not found times in parameters.",
                        status = HttpStatusCode.BadRequest,
                    )

                getVarargApi(call).baz(_times)
                call.response.status(HttpStatusCode.OK)
            }
        }
    }
}