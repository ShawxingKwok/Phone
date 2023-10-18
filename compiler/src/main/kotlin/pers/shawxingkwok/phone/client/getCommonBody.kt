package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
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
private fun KSFunctionDeclaration.getCommonBody(ksclass: KSClassDeclaration) =
    buildString {
        append(getClientFunctionHeader())

        mayEmbrace(
            condition = returnType!!.resolve() != resolver.builtIns.unitType,
            getStart = { append(": ${returnType!!.text}") },
            getEnd = {
                append("\n")
                val returnType = returnType!!.resolve()
                if (returnType.isMarkedNullable) {
                    append("if(response.status == HttpStatusCode.NotFound)\n")
                    append("~return null!~\n\n")
                }
                append("return decode(response.bodyAsText(), ${returnType.getSerializerText()}, ${this@getCommonBody.getCipherTextForReturn(ksclass)})\n")
            },
        ) {
            append(" {\n")
            append("""val response = client.post("/${'$'}basicUrl/${simpleName()}${mayPolymorphicId}")""")
            if (parameters.any()){
                append("{\n")
                append(getJsonParametersBody(ksclass))
                append("\n}")
            }
            append("\n\ncheckNoBadRequest(response)\n")
        }

        append("}")
    }