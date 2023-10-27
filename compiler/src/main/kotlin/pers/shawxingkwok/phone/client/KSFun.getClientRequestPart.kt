package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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

    return buildString {
        append("""
            method = HttpMethod.$methodName
        
            onEachRequest()
            extendRequest?.invoke(this)            
        """.trimStart())

        if (withToken) append("addToken()")

        if (parameters.none()) return@buildString

        append("val parameters = parameters{\n")

        parameters.forEach { param ->
            append("appendWithJson(")
            append("\"${param.name!!.asString()}\", ")
            append("${param.name!!.asString()}, ")
            append("${param.getSerializerText()}, ")
            append("${param.getCipherText(ksclass)})\n")
        }

        append("\n}\n")

        if (withForm) {
            """
            val form = FormDataContent(parameters)
            setBody(form)                            
            """.trimStart().let(::append)
        } else
            append("url.parameters.appendAll(parameters)\n")
    }
}