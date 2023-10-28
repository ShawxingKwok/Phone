package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.client.parts.getClientHeader
import pers.shawxingkwok.phone.client.parts.getClientRequestPart
import pers.shawxingkwok.phone.client.parts.getClientTagStatement
import pers.shawxingkwok.phone.pathEnd

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientManualContent(
    ksclass: KSClassDeclaration,
    kind: Kind.Manual,
    withToken: Boolean,
)
    : String
=
    """
    ${getClientHeader(ksclass)} =
        ~runCatching {
            val response = client.request("${'$'}basicUrl/${ksclass.apiNameInPhone}/$pathEnd") {
                ${getClientRequestPart(ksclass, withToken)}
            }

            response.check()

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