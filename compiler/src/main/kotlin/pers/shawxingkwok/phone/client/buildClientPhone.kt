@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.qualifiedName
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
            inline fun <T: ${Decls().ClientWebSocketSession}> Result<T>.onReceivedSuccess(act: T.() -> Unit){
                onSuccess{ it.act() }
            }
            """.trim()
        }}    
        
        open class Phone(
            val client: HttpClient,
            private val host: String,
            private val port: Int,
            private val usesHttps: Boolean,
            private val usesWss: Boolean,
            private val tokenScheme: String = "Bearer",
            var token: String? = null,
        ) {
            constructor(
                client: HttpClient,
                tokenScheme: String = "Bearer",
                token: String? = null,
            ) :
                ~this(
                    client = client,
                    host = "localhost",
                    port = 80,
                    usesHttps = false,
                    usesWss = false,
                    tokenScheme = tokenScheme,
                    token = token
                )!~
        
            private val basicUrl: String =
                ~buildString {
                    append("http")
                    if (usesHttps) append("s")
                    append("://${'$'}host:${'$'}port")
                }!~
            
            protected open fun HttpRequestBuilder.onEachRequest(apiKClass: KClass<*>) {}
                
            private fun HttpRequestBuilder.enableWssIfNeeded(isRaw: Boolean){
                if(!usesWss) return

                url.protocol = URLProtocol.WSS
                url.port = if(isRaw) port else url.protocol.defaultPort
            }
                
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

context (CodeFormatter)
internal fun KSClassDeclaration.getBody(): String =
    """
    inner class $apiNameInPhone(
        private val extendRequest: (HttpRequestBuilder.() -> Unit)? = null
    )
        ~: ${qualifiedName()}!~ 
    {                    
        ${getNeededFunctions().joinToString("\n\n"){ ksFun ->
            val commonType = ksFun.commonType
            val webSocketAnnot = ksFun.getAnnotationByType(Phone.WebSocket::class)

            val withToken = getAnnotationByType(Phone.Auth::class)?.withToken
                ?: getAnnotationByType(Phone.Auth::class)?.withToken
                ?: false

            when {
                commonType != null -> ksFun.getCommonBody(this, commonType, withToken)
                webSocketAnnot == null -> TODO()
                else -> ksFun.getWebSocketBody(this, webSocketAnnot, withToken)
            }
        }}
    }
    """.trim()