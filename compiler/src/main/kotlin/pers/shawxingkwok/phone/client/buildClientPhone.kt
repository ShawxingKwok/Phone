@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.client.parts.getClientHeader

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
            private val withHttps: Boolean,
            private val withWss: Boolean,
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
                    withHttps = false,
                    withWss = false,
                    tokenScheme = tokenScheme,
                    token = token
                )!~
            
            ${MyProcessor.phones.joinToString("\n\n"){ ksclass ->
                """
                interface ${ksclass.apiNameInPhone} : ${ksclass.qualifiedName()}{
                    ${ksclass.getNeededFunctions().joinToString("\n\n"){ it.getClientHeader(ksclass) }}
                }
                """.trim()
            }}
            
            private val basicUrl: String =
                ~buildString {
                    append("http")
                    if (withHttps) append("s")
                    append("://${'$'}host:${'$'}port")
                }!~
            
            protected open fun HttpRequestBuilder.onEachRequest() {}
                
            private fun HttpRequestBuilder.enableWssIfNeeded(isRaw: Boolean){
                if(!withWss) return

                url.protocol = URLProtocol.WSS
                url.port = if(isRaw) port else url.protocol.defaultPort
            }
                
            private fun HttpRequestBuilder.addToken() {
                checkNotNull(token){
                    "Set token before the request with authentication."
                }
                header(HttpHeaders.Authorization, "${'$'}tokenScheme ${'$'}token")
            }
        
            private inline fun <reified T> appendWithJson(
                key: String,
                value: T,
                serializer: KSerializer<T & Any>?,
                cipher: Phone.Feature.Crypto.Cipher?,
                add: (String, String) -> Unit,
            ){
                if (value == null) return
                val newV = encode(value, serializer, cipher)
                add(key, newV)
            }
        
            private suspend fun HttpResponse.check() {
                check(status == HttpStatusCode.OK || status == HttpStatusCode.NoContent){
                    "${'$'}status ${'$'}{bodyAsText()}"
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
    open fun $apiNameInPhone(extendRequest: (HttpRequestBuilder.() -> Unit)? = null) = object : $apiNameInPhone {                    
        ${getNeededFunctions().joinToString("\n\n"){ ksFun ->
            val withToken = getAnnotationByType(Phone.Feature.Auth::class)?.withToken
                ?: getAnnotationByType(Phone.Feature.Auth::class)?.withToken
                ?: false
    
            when(val kind = ksFun.kind) {
                is Kind.Common -> ksFun.getClientCommonContent(this, kind, withToken)
                is Kind.Manual -> ksFun.getClientManualContent(this, kind, withToken)
                is Kind.PartialContent -> ksFun.getClientPartialContent(this, kind, withToken)
                is Kind.WebSocket -> ksFun.getClientWebSocketContent(this, kind, withToken)
            }
        }}
    }
    """.trim()