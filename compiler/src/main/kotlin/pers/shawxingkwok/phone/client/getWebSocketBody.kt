package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.KtGen
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.getNeededFunctions
import pers.shawxingkwok.phone.insertIf
import pers.shawxingkwok.phone.phoneName

context (KtGen)
internal fun KSClassDeclaration.getWebSocketBody(webSocket: Phone.WebSocket): String {
    val webSocketSessionKSClassText = getDeclText(
        import = "io.ktor.client.plugins.websocket.${insertIf(!webSocket.isRaw){ "Default" }}ClientWebSocketSession",
        innerName = null,
        isTopLevelAndExtensional = false,
    )

    val functionsText = getNeededFunctions()
        .joinToString("\n\n"){
            it.getWebSocketBody(this, webSocket)
        }

    return  """
        inner class $phoneName(
            private val act: suspend ($webSocketSessionKSClassText) -> Unit
        )
            : ${qualifiedName()}
        {
            private val basicUrl = "${"$"}mBasicUrl/${phoneName}" 
            
            $functionsText
        }
        """.trim()
}

context (KtGen)
private fun KSFunctionDeclaration.getWebSocketBody(
    ksclass: KSClassDeclaration,
    webSocket: Phone.WebSocket,
) =
    buildString {
        val path = "${'$'}basicUrl/${simpleName()}$mayPolymorphicId"

        append("""
        ${getClientFunctionHeader()} {
            maySecureWebSocket${insertIf(webSocket.isRaw){ "Raw" }}(
                path = "$path",
                request = {
                    ${newLineIf(webSocket.subProtocol.any()){ "header(HttpHeaders.SecWebSocketProtocol, \"${webSocket.subProtocol}\")" }}
                    ${newLineIf(parameters.any()){ getJsonParametersBody(ksclass) }} 
                },
            ){
                checkNoBadRequest(call.response)
                act(this)
            }
        }
        """.trim())
    }