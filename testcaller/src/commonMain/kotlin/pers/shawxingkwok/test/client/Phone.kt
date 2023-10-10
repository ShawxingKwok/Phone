@file:Suppress("LocalVariableName", "DuplicatedCode")

package pers.shawxingkwok.test.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pers.shawxingkwok.test.model.AllSerializers
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.DeserializationStrategy
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import pers.shawxingkwok.test.api.AccountApi
import kotlin.String
import pers.shawxingkwok.test.model.LoginResult
import kotlin.Long
import pers.shawxingkwok.test.model.User
import pers.shawxingkwok.test.api.ChatApi
import kotlin.collections.List

class Phone(private val client: HttpClient) {
    private companion object {
        const val BASIC_URL = "http://127.0.0.1:8080"
    }

    private fun HttpRequestBuilder.jsonParameter(key: String, value: Any?){
        val newV = encode(value ?: return)
        parameter(key, newV)
    }

    @Suppress("UNCHECKED_CAST")
    private fun encode(value: Any): String {
        if (value is String) return value

        return when(val customSerializer = AllSerializers[value::class] as SerializationStrategy<Any>?){
            null -> Json.encodeToString(value)
            else -> Json.encodeToString(customSerializer, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> decode(text: String): T {
        if (T::class == String::class)
            return text as T

        AllSerializers as Map<KClass<out Any>, KSerializer<Any>>

        return when(val customSerializer = AllSerializers[T::class] as DeserializationStrategy<T>?){
            null -> Json.decodeFromString(text)
            else -> Json.decodeFromString(customSerializer, text)
        }
    }

    val accountApi = object: AccountApi {
        private val mBasicUrl = "${BASIC_URL}/AccountApi"

        override suspend fun login(
            email: String,
            password: String,
            verificationCode: String?,
        ): LoginResult {
            val response = client.get("$mBasicUrl/login") {
                jsonParameter("email", email)
                jsonParameter("password", password)
                jsonParameter("verificationCode", verificationCode)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            return response.bodyAsText().let(::decode)
        }

        override suspend fun delete(id: Long) {
            val response = client.get("$mBasicUrl/delete") {
                jsonParameter("id", id)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
        }

        override suspend fun search(id: Long): User? {
            val response = client.get("$mBasicUrl/search") {
                jsonParameter("id", id)
            }
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            if(response.status != HttpStatusCode.NotFound)
                return null
            return response.bodyAsText().let(::decode)
        }
    }

    val chatApi = object: ChatApi {
        private val mBasicUrl = "${BASIC_URL}/ChatApi"

        override suspend fun getChats(): List<String> {
            val response = client.get("$mBasicUrl/getChats")
            check(response.status != HttpStatusCode.BadRequest){
                response.bodyAsText()
            }
            return response.bodyAsText().let(::decode)
        }
    }
}