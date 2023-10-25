package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.getHeader
import pers.shawxingkwok.phone.insertIf

context (CodeFormatter)
internal fun KSFunctionDeclaration.getWebSocketBody(
    ksclass: KSClassDeclaration,
    webSocketAnnot: Phone.WebSocket,
    withToken: Boolean,
)
    : String
{
    val sessionTypeText =
        if (webSocketAnnot.isRaw)
            Decls().ClientWebSocketSession
        else
            Decls().DefaultClientWebSocketSession

    val sessionFunText =
        if (webSocketAnnot.isRaw)
            Decls().clientWebSocketRawSession
        else
            Decls().clientWebSocketSession

    return """
        ${getHeader("Result<$sessionTypeText>")} =
            ~runCatching{
                client.$sessionFunText(
                    host = host, port = port,
                    path = "${ksclass.apiNameInPhone}/${simpleName()}",
                    block = {
                        onEachRequest(this@${ksclass.apiNameInPhone}::class)
                        extendRequest?.invoke(this)
                        
                        ${getParametersBody(ksclass, "parameter")}
                        
                        ${insertIf(withToken){ "addToken()" }}

                        enableWssIfNeeded(${webSocketAnnot.isRaw})
                    },
                )
            }!~
        """.trim()
}