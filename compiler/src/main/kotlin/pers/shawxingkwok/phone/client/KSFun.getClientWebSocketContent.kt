package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.getHeader

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientWebSocketContent(
    ksclass: KSClassDeclaration,
    kind: Kind.WebSocket,
    withToken: Boolean,
)
    : String
{
    val sessionTypeText =
        if (kind.isRaw)
            Decls().ClientWebSocketSession
        else
            Decls().DefaultClientWebSocketSession

    val sessionFunText =
        if (kind.isRaw)
            Decls().clientWebSocketRawSession
        else
            Decls().clientWebSocketSession

    return """
    ${getHeader("Result<$sessionTypeText>")} =
        ~runCatching{
            client.$sessionFunText(
                host = host, port = port,
                path = "${ksclass.apiNameInPhone}/$pathEnd",
                block = {
                    ${getClientRequestPart(ksclass, withToken)}
                    enableWssIfNeeded(${kind.isRaw})
                },
            )
        }!~
    """.trim()
}