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
    methodInfo: MethodInfo = getMethodInfo(ksclass),
): String {
    val withToken = getAnnotationByType(Phone.Feature.Auth::class)?.withToken
        ?: getAnnotationByType(Phone.Feature.Auth::class)?.withToken
        ?: false

    val (methodName, withForm) = methodInfo

    return """
    method = HttpMethod.$methodName

    ${insertIf(withToken){ "addToken()" }}
    
    ${when{
        parameters.none() -> ""
        
        withForm -> """
            val parameters = parameters{
                ${parameters.getText(ksclass, "::append")}
            }
            val form = FormDataContent(parameters)
            setBody(form)     
        """.trim()
        
        else -> parameters.getText(ksclass, "::parameter")
    }}
                   
    extendRequest?.invoke(this)            
    onEachRequest()
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