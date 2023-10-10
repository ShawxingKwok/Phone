@file:Suppress("LocalVariableName", "DuplicatedCode")

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
import kotlin.String
import pers.shawxingkwok.test.model.LoginResult
import kotlin.Long
import pers.shawxingkwok.test.model.User
import pers.shawxingkwok.test.api.ChatApi
import kotlin.collections.List
import pers.shawxingkwok.test.api.TimeApi
import pers.shawxingkwok.test.model.Time
import pers.shawxingkwok.test.model.TimeSerializer

class Phone(private val client: HttpClient) {
    private companion object {
        const val BASIC_URL = "http://127.0.0.1:8080"
    }

    @Suppress("UNCHECKED_CAST")
    private fun HttpRequestBuilder.jsonParameter(
        key: String,
        value: Any?,
        serializer: KSerializer<out Any>?
    ){
        if (value == null) return
        val newV = encode(value, serializer as KSerializer<Any>?)
        parameter(key, newV)
    }

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
    }
}