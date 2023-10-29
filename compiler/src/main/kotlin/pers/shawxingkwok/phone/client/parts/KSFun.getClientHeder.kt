package pers.shawxingkwok.phone.client.parts

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.Decls
import pers.shawxingkwok.phone.Call
import pers.shawxingkwok.phone.getHeader
import pers.shawxingkwok.phone.getCall

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientHeader(ksclass: KSClassDeclaration): String{
    val innerText = when(val kind = getCall(ksclass)){
        is Call.Common -> kind.returnType.text

        is Call.Manual ->
            if (kind.tagType == resolver.builtIns.unitType)
                "HttpResponse"
            else
                "Pair<${kind.tagType.text}, HttpResponse>"

        is Call.WebSocket ->
            if (kind.isRaw)
                Decls().ClientWebSocketSession
            else
                Decls().DefaultClientWebSocketSession

        is Call.PartialContent ->
            "PartialContentHandler<${kind.tagType.text}>"
    }

    return getHeader("Result<$innerText>")
}