package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
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

    val webSocketKSFunText = getDeclText(
        import =
            if (webSocket.isRaw)
                "io.ktor.client.plugins.websocket.cio.webSocketRaw"
            else
                "io.ktor.client.plugins.websocket.webSocket",
        innerName = null,
        isTopLevelAndExtensional = true,
    )

    return  """
        inner class $phoneName(
            private val act: suspend ($webSocketSessionKSClassText) -> Unit
        )
            : ${qualifiedName()}
        {
            ${getNeededFunctions().joinToString("\n\n"){ ksfun ->
                val withToken =
                    ksfun.getAnnotationByType(Phone.Auth::class)?.withToken 
                    ?: this.getAnnotationByType(Phone.Auth::class)?.withToken
                    ?: false

                """
                ${ksfun.getClientFunctionHeader()} {
                    client.$webSocketKSFunText(
                        host = host, port = port,
                        path = "/$phoneName/${ksfun.simpleName()}${ksfun.mayPolymorphicId}",
                        request = webSocketRequest($withToken){
                            ${insertIf(webSocket.subProtocol.any()){ 
                                "header(HttpHeaders.SecWebSocketProtocol, \"${webSocket.subProtocol}\")" 
                            }}
                
                            ${insertIf(ksfun.parameters.any()){ 
                                ksfun.getParametersBody(this, "jsonParameter") 
                            }}
                        },
                        block = act,
                    )
                }
                """.trim()
            }}
        }
        """.trim()
}