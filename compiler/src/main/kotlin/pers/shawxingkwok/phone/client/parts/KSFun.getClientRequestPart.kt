package pers.shawxingkwok.phone.client.parts

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.getSerializerText

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientRequestPart(
    ksclass: KSClassDeclaration,
    methodName: String = getMethodName(ksclass)
): String {
    val withToken = getAnnotationByType(Phone.Feature.Auth::class)?.withToken
        ?: getAnnotationByType(Phone.Feature.Auth::class)?.withToken
        ?: false

    return """
    onRequestStart?.invoke(this)            

    method = HttpMethod.$methodName

    ${insertIf(withToken){ "addToken()" }}
    
    ${insertIf(parameters.any()) {
        """
        addParameters(${kind is Kind.WebSocket}){
            ${parameters.getText(ksclass)}
        }
        """.trim()
    }}

    onEachRequestEnd()
    """.trim()
}

context (CodeFormatter)
private fun List<KSValueParameter>.getText(ksclass: KSClassDeclaration) =
    buildString {
        this@getText.forEach { param ->
            append("appendWithJson(")
            append("\"${param.name!!.asString()}\", ")
            append("${param.name!!.asString()}, ")
            append("${param.getSerializerText()}, ")
            append("${param.getCipherText(ksclass)})\n")
        }
        removeSuffix("\n")
    }