package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*

context (CodeFormatter)
internal fun KSClassDeclaration.getCommonBody(): String =
    """
    val ${phoneName.replaceFirstChar(Char::lowercase)} = object : $text {                    
        private val basicUrl = "${"$"}mBasicUrl/${phoneName}" 
    
        ${getNeededFunctions().joinToString("\n\n"){ it.getCommonBody(this) }}
    }
    """.trim()

context (CodeFormatter)
private fun KSFunctionDeclaration.getCommonBody(ksclass: KSClassDeclaration): String {
    val withToken = getAnnotationByType(Phone.Auth::class)?.withToken
        ?: ksclass.getAnnotationByType(Phone.Auth::class)?.withToken
        ?: false

    val returnType = returnType!!.resolve()
    val hasReturn = returnType != resolver.builtIns.unitType

    return """
        ${getClientFunctionHeader()}${insertIf(hasReturn) { ": ${returnType.text}" }}{
            val response = client.submitForm(
                url = "${'$'}basicUrl/${simpleName()}${mayPolymorphicId}",
                formParameters = parameters {
                    ${getParametersBody(ksclass, "appendWithJson")}
                },
                encodeInQuery = ${getMethod(ksclass) == Method.GET},
            ){
                additionalRequest?.invoke(this)
                ${insertIf(withToken){ "addToken(this)" }}
            }
                
            ${insertIf(returnType.isMarkedNullable) {
                """
                if(response.status == HttpStatusCode.NotFound)
                    ~return null!~
                """.trim()
            }}
                
            checkResponse(response)
                
            ${insertIf(hasReturn) {
                val serializerText = returnType.getSerializerText()
                "return decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})"
            }}
        }
        """
}