package pers.shawxingkwok.phone.client.parts

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.Decls
import pers.shawxingkwok.phone.Kind
import pers.shawxingkwok.phone.getHeader
import pers.shawxingkwok.phone.kind

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientHeader(ksclass: KSClassDeclaration): String{
    val innerText = when(val kind = kind){
        is Kind.Common -> kind.returnType.text

        is Kind.Manual ->
            if (kind.tagType == resolver.builtIns.unitType)
                "HttpResponse"
            else
                "Pair<${kind.tagType.text}, HttpResponse>"

        is Kind.WebSocket ->
            if (kind.isRaw)
                Decls().ClientWebSocketSession
            else
                Decls().DefaultClientWebSocketSession

        is Kind.PartialContent ->
            "PartialContentHandler<${kind.tagType.text}>>"
    }

    return getHeader("Result<$innerText>")
}