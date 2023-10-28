package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.getSerializerText

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientRequestPart(
    ksclass: KSClassDeclaration,
    withToken: Boolean,
    methodInfo: MethodInfo = getMethodInfo(ksclass),
): String {
    val (methodName, withForm) = methodInfo

    return """
    method = HttpMethod.$methodName
               
    onEachRequest()
    extendRequest?.invoke(this)            
    
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