package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Kind
import pers.shawxingkwok.phone.apiPropNameInPhone
import pers.shawxingkwok.phone.getMethodInfo
import pers.shawxingkwok.phone.pathEnd
import pers.shawxingkwok.phone.server.parts.getServerParametersPart

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerPartialContent(
    ksclass: KSClassDeclaration,
    kind: Kind.PartialContent,
): String{
    val (methodName, withForm) = getMethodInfo(ksclass)
    val start = methodName.replaceFirstChar { it.lowercase() }
    val tagType = kind.tagType

    return buildString {
        """
        $start("/$pathEnd"){
        """.trimStart()
            .let(::append)

        if (parameters.any())
            append("val params = call.request.queryParameters\n\n")

        append("val (tag, file) = ")

        val invokePart = buildString {
            append("${ksclass.apiPropNameInPhone}.${simpleName()}(")
            append(getServerParametersPart(ksclass, start))
            append(")()\n\n")
        }
        append(invokePart)

        run {
            if (tagType == resolver.builtIns.unitType) return@run

            if (tagType.isMarkedNullable)
                append("if (tag != null){\n")

            """
            val text = encode(tag, ${tagType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})
            call.response.header("Phone-Tag", text)
            """
            .trimStart().let(::append)

            if (tagType.isMarkedNullable)
                append("}\n")
        }

        append("call.response.status(HttpStatusCode.OK)\n")
        append("call.respondFile(file)\n")
        append("}")
    }
}