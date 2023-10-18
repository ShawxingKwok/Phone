@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Args
import pers.shawxingkwok.phone.getCoderFunctions
import pers.shawxingkwok.phone.insertIf
import pers.shawxingkwok.phone.mayEmbrace

internal fun buildClientPhone(phones: List<KSClassDeclaration>) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ClientPackageName,
        dependencies = Dependencies(true, *phones.map{ it.containingFile!! }.toTypedArray()),
        fileName = "Phone",
        extensionName = "",
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
        class Phone(
            private val client: HttpClient,
            private val mBasicUrl: String = "http://localhost:80",
        ) {
            ${insertIf(phones.any { it.isAnnotationPresent(Phone.WebSocket::class) }){
                """
                private val webSocketSecure = mBasicUrl.startsWith("https:")
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
                """               
            }}
            
            ${insertIf(phones.any { it.getAnnotationByType(Phone.WebSocket::class)?.isRaw == false }){
                """
                private suspend fun maySecureWebSocket(
                    path: String,
                    request: HttpRequestBuilder.() -> Unit,
                    act: suspend DefaultClientWebSocketSession.() -> Unit,
                ) {
                    client.${getDeclText("io.ktor.client.plugins.websocket.webSocket", null, true)}(
                        host = host, port = port,
                        path = path,
                        request = {
                            if (webSocketSecure) {
                                url.protocol = URLProtocol.WSS
                                url.port = port
                            }
                            request()
                        },
                        block = act
                    )
                }
                """
            }}
            
            ${insertIf(phones.any { it.getAnnotationByType(Phone.WebSocket::class)?.isRaw == true }){
                """
                private suspend fun maySecureWebSocketRaw(
                    path: String,
                    request: HttpRequestBuilder.() -> Unit,
                    act: suspend ClientWebSocketSession.() -> Unit,
                ){
                    client.${getDeclText("io.ktor.client.plugins.websocket.cio.webSocketRaw", null, true)}(
                        method = HttpMethod.Post,
                        host = host, port = port,
                        path = path,
                        request = {
                            if (webSocketSecure) {
                                url.protocol = URLProtocol.WSS
                                url.port = port
                            }
                            request()
                        },
                        block = act
                    )
                }
                """
            }}
            
            ${insertIf(phones.any { it.isAnnotationPresent(Phone.Api::class) }){
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
    
            private suspend fun checkNoBadRequest(response: HttpResponse){
                check(response.status != HttpStatusCode.BadRequest){
                    response.bodyAsText()
                }
            }
            
            ${getCoderFunctions()}
            
            ${phones.joinToString("\n\n") { ksClass ->
                when(val webSocket = ksClass.getAnnotationByType(Phone.WebSocket::class)) {
                    null -> ksClass.getCommonBody()
                    else -> ksClass.getWebSocketBody(webSocket)
                }
            }}    
        }
        """
    }
}