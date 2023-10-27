package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.*

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientCommonContent(
    ksclass: KSClassDeclaration,
    kind: Kind.Common,
    withToken: Boolean,
)
    : String
=
    """
    ${getHeader("Result<${kind.returnType.text}>")} =
        ~runCatching{
            val response = client.request("${'$'}basicUrl/${ksclass.apiNameInPhone}/$pathEnd"){
                ${getClientRequestPart(ksclass, withToken)}
            }
            ${insertIf(kind.returnType.isMarkedNullable) {
               """
                if(response.status == HttpStatusCode.NotFound)
                    ~return@runCatching null!~
                """.trim()
            }}
                
            response.checkIsOK()
                
            ${insertIf(kind.returnType != resolver.builtIns.unitType) {
                val serializerText = kind.returnType.getSerializerText()
                "decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})"
            }}
        }!~
    """