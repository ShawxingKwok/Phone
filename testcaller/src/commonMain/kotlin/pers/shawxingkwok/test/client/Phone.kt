package pers.shawxingkwok.test.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.api.AccountApi
import pers.shawxingkwok.test.model.LoginResult
import pers.shawxingkwok.test.model.User
import pers.shawxingkwok.test.api.ChatApi
import pers.shawxingkwok.test.api.TimeApi
import pers.shawxingkwok.test.model.Time
import pers.shawxingkwok.test.serializers.TimeSerializer
import pers.shawxingkwok.test.serializers.TimeArraySerializer

class Phone(private val client: HttpClient) {
    private companion object {
        const val BASIC_URL = "http://127.0.0.1:8080"
    }

    private inline fun <reified T> HttpRequestBuilder.jsonParameter(
        key: String,
        value: T,
        serializer: KSerializer<T & Any>?,
        cipher: Phone.Cipher?,
    ){
        if (value == null) return
        val newV = encode(value, serializer, cipher)
        parameter(key, newV)
    }

    private suspend fun checkNoBadRequest(response: HttpResponse){
        check(response.status != HttpStatusCode.BadRequest){
            response.bodyAsText()
        }
    }

    private inline fun <reified T: Any> encode(
        value: T,
        serializer: KSerializer<T>?,
        cipher: Phone.Cipher?,
    ): String =
        when{
            value is String -> value
            serializer == null -> Json.encodeToString(value)
            else -> Json.encodeToString(serializer, value)
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
            T::class == String::class -> newText as T
            serializer == null -> Json.decodeFromString(newText)
            else -> Json.decodeFromString(serializer, newText)
        }
    }

    val accountApi = object : AccountApi {
        private val mBasicUrl = "${BASIC_URL}/AccountApi"

        override suspend fun login(email: String, password: String): LoginResult {
            val response = client.post("$mBasicUrl/login") {
                jsonParameter("email", email, null, null)
                jsonParameter("password", password, null, null)
            }

            checkNoBadRequest(response)

            return decode(response.bodyAsText(), null, null)
        }

        override suspend fun delete(id: Long) {
            val response = client.post("$mBasicUrl/delete") {
                jsonParameter("id", id, null, null)
            }

            checkNoBadRequest(response)
        }

        override suspend fun delete(id: String) {
            val response = client.post("$mBasicUrl/delete1") {
                jsonParameter("id", id, null, null)
            }

            checkNoBadRequest(response)
        }

        override suspend fun search(id: Long): User? {
            val response = client.post("$mBasicUrl/search") {
                jsonParameter("id", id, null, null)
            }

            checkNoBadRequest(response)

            if(response.status != HttpStatusCode.NotFound)
                return null

            return decode(response.bodyAsText(), null, null)
        }
    }

    val chatApi = object : ChatApi {
        private val mBasicUrl = "${BASIC_URL}/ChatApi"

        override suspend fun getChats(): List<String> {
            val response = client.post("$mBasicUrl/getChats")

            checkNoBadRequest(response)

            return decode(response.bodyAsText(), null, null)
        }
    }

    val timeApi = object : TimeApi {
        private val mBasicUrl = "${BASIC_URL}/TimeApi"

        override suspend fun getTime(): Time {
            val response = client.post("$mBasicUrl/getTime")

            checkNoBadRequest(response)

            return decode(response.bodyAsText(), TimeSerializer, null)
        }

        override suspend fun sumTime(vararg times: Time): Time {
            val response = client.post("$mBasicUrl/sumTime") {
                jsonParameter("times", times, TimeArraySerializer, null)
            }

            checkNoBadRequest(response)

            return decode(response.bodyAsText(), TimeSerializer, null)
        }
    }
}