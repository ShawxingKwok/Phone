package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Call
import pers.shawxingkwok.phone.getPathEnd
import pers.shawxingkwok.phone.server.parts.getServerParametersPart

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerWebSocketContent(
    ksclass: KSClassDeclaration,
    call: Call.WebSocket,
)
    : String
{
    val start =
        if (call.isRaw)
            Decls().serverWebSocketRaw
        else
            Decls().serverWebSocket

    val paramsContent = getServerParametersPart(ksclass, start){ "unacceptedClose(\"$it\")" }

    return buildString {
        """
        $start("/${getPathEnd(ksclass)}"){
        """.trimStart()
            .let(::append)

        if (parameters.any())
            append("val params = call.request.queryParameters\n\n")

        append("${ksclass.apiPropNameInPhone}.${simpleName()}($paramsContent)()\n")

        append("}")
    }
}