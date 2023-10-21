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
            internal val client: HttpClient,
            private val mBasicUrl: String = "http://localhost:80",
        ) {
            private var authorization: String? = null
        
            fun setAuthorization(token: String, scheme: String = "Bearer"){
                this.authorization = "${'$'}scheme ${'$'}token"
            }

            ${insertIf(MyProcessor.phones.any { it.isAnnotationPresent(Phone.WebSocket::class) }){
                """
                private val securesWebSockets = mBasicUrl.startsWith("https:")
                private val host = mBasicUrl.substringBeforeLast(":").substringAfter("://")
                private val port = mBasicUrl.substringAfterLast(":").toIntOrNull() ?: error(TODO())
                            
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
                    
                    if (securesWebSockets) {
                        builder.url.protocol = URLProtocol.WSS
                        builder.url.port = port
                    }
                    
                    if (withToken)
                        ~builder.header(HttpHeaders.Authorization, authorization)!~
                        
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