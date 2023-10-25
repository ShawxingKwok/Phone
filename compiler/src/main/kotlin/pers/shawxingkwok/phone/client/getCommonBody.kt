package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.*

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
            
            when {
                commonType != null -> ksFun.getCommonBody(this, commonType)
                webSocketAnnot == null -> TODO()
                else -> ksFun.getWebSocketBody()    
            }
        }}
    }
    """.trim()

context (CodeFormatter)
private fun KSFunctionDeclaration.getCommonBody(
    ksclass: KSClassDeclaration,
    commonType: KSType,
)
    : String
{
    val withToken = getAnnotationByType(Phone.Auth::class)?.withToken
        ?: ksclass.getAnnotationByType(Phone.Auth::class)?.withToken
        ?: false

    return """
        ${getHeader("Result<${commonType.text}>")} =
            ~runCatching{
                val response = client.submitForm(
                    url = "${'$'}basicUrl/${ksclass.apiNameInPhone}/${simpleName()}${mayPolymorphicId}",
                    formParameters = parameters {
                        ${getParametersBody(ksclass, "appendWithJson")}
                    },
                    encodeInQuery = ${getOrPost(ksclass) == "get"},
                ){
                    onEachRequest(this@${ksclass.apiNameInPhone}::class)
                    extendRequest?.invoke(this)
                    ${insertIf(withToken){ "addToken(this)" }}
                }
                    
                ${insertIf(commonType.isMarkedNullable) {
                    """
                    if(response.status == HttpStatusCode.NotFound)
                        ~return null!~
                    """.trim()
                }}
                    
                checkResponse(response)
                    
                ${insertIf(commonType != resolver.builtIns.unitType) {
                    val serializerText = commonType.getSerializerText()
                    "decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})"
                }}
            }!~
        """
}