@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.getAnnotationByType
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
                "pers.shawxingkwok.phone.Phone",
            )
    ) {
        """
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

            private val host = basicUrl.substringBeforeLast(":").substringAfter("://")
            private val port = basicUrl.substringAfterLast(":").toInt()
            private val securesWebSockets = basicUrl.startsWith("https:")

            private fun addToken(builder: HttpRequestBuilder){
                checkNotNull(token){
                    "Set token before the request with authentication."
                }
                builder.header(HttpHeaders.Authorization, "${'$'}tokenScheme ${'$'}token")
            }
    
            ${insertIf(MyProcessor.phones.any { it.isAnnotationPresent(Phone.WebSocket::class) }){
                """
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
                
                private fun webSocketRequest(
                    withToken: Boolean, 
                    request: HttpRequestBuilder.() -> Unit
                ) = { 
                    builder: HttpRequestBuilder ->
                    
                    additionalRequest?.invoke(builder)
                    
                    if (securesWebSockets) {
                        builder.url.protocol = URLProtocol.WSS
                        builder.url.port = port
                    }
                    
                    if (withToken) addToken(builder)
                        
                    builder.request()
                }
                """               
            }}
            
            ${insertIf(MyProcessor.phones.any { it.isAnnotationPresent(Phone.Api::class) }){
                """
                private inline fun <reified T> ParametersBuilder.appendWithJson(
                    key: String,
                    value: T,
                    serializer: KSerializer<T & Any>?,
                    cipher: Phone.Cipher?,
                ){
                    if (value == null) return
                    val newV = encode(value, serializer, cipher)
                    append(key, newV)
                }
                """
            }}
            
            private suspend fun checkResponse(response: HttpResponse){
                check(response.status == HttpStatusCode.OK){
                    response.bodyAsText()
                }
            }
            
            ${getCoderFunctions()}
            
            ${MyProcessor.phones.joinToString("\n\n") { ksClass ->
                when(val webSocket = ksClass.getAnnotationByType(Phone.WebSocket::class)) {
                    null -> ksClass.getCommonBody()
                    else -> ksClass.getWebSocketBody(webSocket)
                }
            }}    
        }
        """
    }
}