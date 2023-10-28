package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Kind
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.client.parts.getClientHeader
import pers.shawxingkwok.phone.client.parts.getClientRequestPart
import pers.shawxingkwok.phone.client.parts.getClientTagStatement
import pers.shawxingkwok.phone.pathEnd

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientPartialContent(
    ksclass: KSClassDeclaration,
    kind: Kind.PartialContent,
    withToken: Boolean,
)
    : String
{
    val path = "\$basicUrl/${ksclass.apiNameInPhone}/$pathEnd"

    return """
    ${getClientHeader(ksclass)} =
        ~runCatching {
            val response = client.request("$path") {
                ${getClientRequestPart(ksclass, withToken, MethodInfo("head", false))}
            }
            
            response.checkIsOK()
            
            ${if (kind.tagType == resolver.builtIns.unitType)    
                "val tag = Unit"
            else  
                getClientTagStatement(ksclass, kind.tagType)
            }            
            
            PartialContentHandler(tag) { ranges ->
                client.request("$path"){
                    ${getClientRequestPart(ksclass, withToken)}
                    
                    if (ranges.none()) return@request

                    val v = ranges.joinToString(prefix = "bytes="){ range ->
                        "${'$'}{range.first}-${'$'}{range.last}"
                    }
                    header(HttpHeaders.Range, v)
                }
            }
        }!~
    """.trim()
}