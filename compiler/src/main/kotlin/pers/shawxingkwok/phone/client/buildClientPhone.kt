@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone.client

import pers.shawxingkwok.phone.*

internal fun buildClientPhone() {
    createFile(
        phones = MyProcessor.phones,
        packageName = Args.ClientPackageName,
        initialImports =
            setOf(
                "io.ktor.client.*",
                "io.ktor.client.request.*",
                "io.ktor.http.*",
                "io.ktor.client.request.forms.*",
                "io.ktor.client.statement.*",
                "kotlinx.serialization.encodeToString",
                "kotlinx.serialization.json.Json",
                "kotlinx.serialization.KSerializer",
                "kotlinx.serialization.builtins.ByteArraySerializer",
                "kotlin.reflect.KClass",
                "pers.shawxingkwok.phone.Phone",
            )
    ) {
        """
        ${insertIf(MyProcessor.hasWebSocket){
            """
            inline fun <T: ${Decls().WebSocketClientSession}> Result<T>.onReceivedSuccess(act: T.() -> Unit){
                onSuccess{ it.act() }
            }
            """.trim()
        }}    
        
        open class Phone(
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
        
            ${insertIf(MyProcessor.hasWebSocket){
                """
                private val host = basicUrl.substringBeforeLast(":").substringAfter("://")
                private val port = basicUrl.substringAfterLast(":").toInt()
                private val securesWebSockets = basicUrl.startsWith("https:")
                """
            }}
            
            protected open fun HttpRequestBuilder.onEachRequest(apiKClass: KClass<*>) {}
        
            private fun HttpRequestBuilder.addToken() {
                checkNotNull(token){
                    "Set token before the request with authentication."
                }
                header(HttpHeaders.Authorization, "${'$'}tokenScheme ${'$'}token")
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
            
            ${getCoderFunctions()}
            
            ${MyProcessor.phones.joinToString("\n\n") { it.getBody() }}    
        }
        """
    }
}