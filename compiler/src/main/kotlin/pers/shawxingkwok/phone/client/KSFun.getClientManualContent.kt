package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.getHeader
import pers.shawxingkwok.phone.pathEnd

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientManualContent(
    ksclass: KSClassDeclaration,
    kind: Kind.Manual,
    withToken: Boolean,
)
    : String
{
    val returnedText =
        if (kind.tagType == resolver.builtIns.unitType)
            "HttpResponse"
        else
            "Result<Pair<${kind.tagType.text}, HttpResponse>>"

    return """
    ${getHeader(returnedText)} =
        ~runCatching{
            val response = client.request("${'$'}basicUrl/${ksclass.apiNameInPhone}/$pathEnd") {
                ${getClientRequestPart(ksclass, withToken)}
            }

            response.checkIsOK()

            ${if (kind.tagType == resolver.builtIns.unitType)
                "response"
            else { 
                """
                ${getClientTagStatement(ksclass, kind.tagType)}
                    
                tag to response
                """.trimIndent()
           }}
    }!~
    """.trim()
}