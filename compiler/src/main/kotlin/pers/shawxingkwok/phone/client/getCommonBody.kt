package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.isAnnotationPresent
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
private fun KSFunctionDeclaration.getCommonBody(ksclass: KSClassDeclaration) =
    buildString {
        append(getClientFunctionHeader())

        val withToken = getAnnotationByType(Phone.Auth::class)?.withToken
            ?: ksclass.getAnnotationByType(Phone.Auth::class)?.withToken
            ?: false

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
                val serializerText = returnType.getSerializerText()
                append("return decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})\n")
            },
        ) {
            append(" {\n")

            """
            val response = client.submitForm(
                url = "${'$'}basicUrl/${simpleName()}${mayPolymorphicId}",
                formParameters = parameters {
                    ${getParametersBody(ksclass, "appendWithJson")}
                },
                encodeInQuery = ${ getMethod(ksclass) == Method.GET },
            )${insertIf(withToken){
                """
                {
                    header(HttpHeaders.Authorization, authorization)
                }
                """.trim()
            }}
            
            checkNoBadRequest(response)
            """
            .let(::append)
        }

        append("}")
    }