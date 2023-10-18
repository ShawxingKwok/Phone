package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.getNeededFunctions
import pers.shawxingkwok.phone.insertIf
import pers.shawxingkwok.phone.phoneName

context (CodeFormatter)
internal fun KSClassDeclaration.getWebSocketBody(webSocket: Phone.WebSocket): String {
    val webSocketSessionKSClassText = getDeclText(
        import = "io.ktor.client.plugins.websocket.${insertIf(!webSocket.isRaw){ "Default" }}ClientWebSocketSession",
        innerName = null,
        isTopLevelAndExtensional = false,
    )

    return  """
        inner class $phoneName(
            private val act: suspend ($webSocketSessionKSClassText) -> Unit
        )
            : ${qualifiedName()}
        {
            ${getNeededFunctions().joinToString("\n\n"){ ksfun ->
                """
                ${ksfun.getClientFunctionHeader()} {
                    maySecureWebSocket${insertIf(webSocket.isRaw){ "Raw" }}(
                    path = "${'$'}mBasicUrl/$phoneName/${ksfun.simpleName()}${ksfun.mayPolymorphicId}",
                    request = {
                        ${insertIf(webSocket.subProtocol.any()){ "header(HttpHeaders.SecWebSocketProtocol, \"${webSocket.subProtocol}\")" }}
            
                        ${insertIf(ksfun.parameters.any()){ ksfun.getJsonParametersBody(this) }}
                    },
                ){
                    checkNoBadRequest(call.response)
                    act(this)
                }
                }
                """.trim()
            }}
        }
        """.trim()
}