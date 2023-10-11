@file:Suppress("LocalVariableName", "DuplicatedCode", "SameParameterValue")

package pers.shawxingkwok.test.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.DeserializationStrategy
import pers.shawxingkwok.test.api.AccountApi
import pers.shawxingkwok.test.model.LoginResult
import pers.shawxingkwok.test.model.User
import pers.shawxingkwok.test.api.ChatApi
import pers.shawxingkwok.test.api.TimeApi
import pers.shawxingkwok.test.model.Time
import pers.shawxingkwok.test.model.TimeSerializer
import pers.shawxingkwok.test.model.TimeArraySerializer

class Phone(private val client: HttpClient) {
    private companion object {
        const val BASIC_URL = "http://127.0.0.1:8080"
    }

    private inline fun <reified T> HttpRequestBuilder.jsonParameter(
        key: String,
        value: T,
        serializer: KSerializer<T & Any>?
    ){
        if (value == null) return
        val newV = encode(value, serializer)
        parameter(key, newV)
    }

    private inline fun <reified T: Any> encode(
        value: T,
        serializer: KSerializer<T>?,
    ): String =
        when{
            value is String -> value
            serializer == null -> Json.encodeToString(value)
            else -> Json.encodeToString(serializer, value)
        }

    private inline fun <reified T: Any> decode(
        text: String,
        serializer: KSerializer<T>?
    ): T =
        when{
            T::class == String::class -> text as T
            serializer == null -> Json.decodeFromString(text)
            else -> Json.decodeFromString(serializer, text)
        }

    val accountApi = object : AccountApi {
        private val mBasicUrl = "${BASIC_URL}/AccountApi"

        override suspend fun login(
            email: String,
            password: String,
            verificationCode: String?,
        ): LoginResult {
            val response = client.get("$mBasicUrl/login") {
                jsonParameter("email", email, null)
                jsonParameter("password", password, null)
                jsonParameter("verificationCode", verificationCode, null)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            val text = response.bodyAsText()
            return decode(text, null)
        }

        override suspend fun delete(id: Long) {
            val response = client.get("$mBasicUrl/delete") {
                jsonParameter("id", id, null)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
        }

        override suspend fun search(id: Long): User? {
            val response = client.get("$mBasicUrl/search") {
                jsonParameter("id", id, null)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            if(response.status != HttpStatusCode.NotFound)
                return null
            val text = response.bodyAsText()
            return decode(text, null)
        }
    }

    val chatApi = object : ChatApi {
        private val mBasicUrl = "${BASIC_URL}/ChatApi"

        override suspend fun getChats(): List<String> {
            val response = client.get("$mBasicUrl/getChats")
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            val text = response.bodyAsText()
            return decode(text, null)
        }
    }

    val timeApi = object : TimeApi {
        private val mBasicUrl = "${BASIC_URL}/TimeApi"

        override suspend fun getTime(): Time {
            val response = client.get("$mBasicUrl/getTime")
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            val text = response.bodyAsText()
            return decode(text, TimeSerializer)
        }

        override suspend fun sumTime(vararg times: Time): Time {
            val response = client.get("$mBasicUrl/sumTime") {
                jsonParameter("times", times, TimeArraySerializer)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            val text = response.bodyAsText()
            return decode(text, TimeSerializer)
        }
    }
}