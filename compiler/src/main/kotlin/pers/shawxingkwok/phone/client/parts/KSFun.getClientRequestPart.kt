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
    extendRequest?.invoke(this)            

    method = HttpMethod.$methodName

    ${insertIf(withToken){ "addToken()" }}
    
    ${when{
        parameters.none() -> ""

        methodName == "Get" 
        || methodName == "Head" 
        || kind is Kind.WebSocket -> parameters.getText(ksclass, "::parameter")
        
        else -> """
            if (body !== ${Decls().EmptyContent}) {
                ${parameters.getText(ksclass, "::parameter")}
            } else {
                val parameters = parameters{
                    ${parameters.getText(ksclass, "::append")}
                }
                val form = FormDataContent(parameters)
                setBody(form)     
            }
        """.trim()
    }}

    onEachRequestEnd()
    """.trim()
}

context (CodeFormatter)
private fun List<KSValueParameter>.getText(ksclass: KSClassDeclaration, funText: String) =
    buildString {
        this@getText.forEach { param ->
            append("appendWithJson(")
            append("\"${param.name!!.asString()}\", ")
            append("${param.name!!.asString()}, ")
            append("${param.getSerializerText()}, ")
            append("${param.getCipherText(ksclass)}, ")
            append("$funText)\n")
        }
        removeSuffix("\n")
    }