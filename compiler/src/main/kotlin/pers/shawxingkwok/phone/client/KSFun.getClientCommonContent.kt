package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.client.parts.getClientHeader
import pers.shawxingkwok.phone.client.parts.getClientRequestPart

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientCommonContent(
    ksclass: KSClassDeclaration,
    kind: Kind.Common,
    withToken: Boolean,
)
    : String
=
    """
    ${getClientHeader(ksclass)} =
        ~runCatching {
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