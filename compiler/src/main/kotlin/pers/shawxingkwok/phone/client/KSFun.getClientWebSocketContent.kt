package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.client.parts.getClientHeader
import pers.shawxingkwok.phone.client.parts.getClientRequestPart

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientWebSocketContent(
    ksclass: KSClassDeclaration,
    kind: Kind.WebSocket,
    withToken: Boolean,
)
    : String
{
    val sessionFunText =
        if (kind.isRaw)
            Decls().clientWebSocketRawSession
        else
            Decls().clientWebSocketSession

    return """
    ${getClientHeader(ksclass)} =
        ~runCatching {
            client.$sessionFunText(
                host = host, port = port,
                path = "${ksclass.apiNameInPhone}/$pathEnd",
            ){
                enableWssIfNeeded(${kind.isRaw})
                ${getClientRequestPart(ksclass, withToken)}
            }
        }!~
    """.trim()
}