package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.*

context (CodeFormatter)
internal fun KSFunctionDeclaration.getCommonBody(
    ksclass: KSClassDeclaration,
    commonType: KSType,
    withToken: Boolean,
)
    : String
=
    """
    ${getHeader("Result<${commonType.text}>")} =
        ~runCatching{
            val response = client.submitForm(
                url = "${'$'}basicUrl/${ksclass.apiNameInPhone}/${simpleName()}${mayPolymorphicId}",
                formParameters = parameters {
                    ${getParametersBody(ksclass, "append")}
                },
                encodeInQuery = ${getOrPost(ksclass) == "get"},
            ){
                onEachRequest(this@${ksclass.apiNameInPhone}::class)
                extendRequest?.invoke(this)
                ${insertIf(withToken){ "addToken()" }}
            }
                
            ${insertIf(commonType.isMarkedNullable) {
                """
                if(response.status == HttpStatusCode.NotFound)
                    ~return@runCatching null!~
                """.trim()
            }}
                
            response.checkIsOK()
                
            ${insertIf(commonType != resolver.builtIns.unitType) {
                val serializerText = commonType.getSerializerText()
                "decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})"
            }}
        }!~
    """