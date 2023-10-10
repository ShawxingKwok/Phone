@file:Suppress("LocalVariableName", "DuplicatedCode")

package pers.shawxingkwok.test.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.String
import kotlin.Long

object Phone{
    abstract class AccountApi(val call: ApplicationCall) : pers.shawxingkwok.test.api.AccountApi
    abstract class ChatApi(val call: ApplicationCall) : pers.shawxingkwok.test.api.ChatApi

    private fun encode(value: Any): String =
        if (value is String) value
        else Json.encodeToString(value)

    private inline fun <reified T> decode(text: String): T =
        if(T::class == String::class) text as T
        else Json.decodeFromString(text)

    fun configure(
        routing: Routing,
        getAccountApi: (ApplicationCall) -> AccountApi,
        getChatApi: (ApplicationCall) -> ChatApi,
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
                call.respondText(encode(ret), status = HttpStatusCode.OK)
            }

            get("/delete"){
                val params = call.request.queryParameters

                val _id: Long = params["id"]
                    ?.let{
                        try {
                            decode(it)
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
                            decode(it)
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
                    
                when(val ret = getAccountApi(call).search(_id)){
                    null -> call.response.status(HttpStatusCode.NotFound)
                    else -> call.respondText(encode(ret), status = HttpStatusCode.OK)
                }
            }
        }

        routing.route("ChatApi"){
            get("/getChats"){
                val ret = getChatApi(call).getChats()
                call.respondText(encode(ret), status = HttpStatusCode.OK)
            }
        }
    }
}