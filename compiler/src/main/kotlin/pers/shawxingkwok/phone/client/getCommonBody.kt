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
                encodeInQuery = ${when{
                    this@getCommonBody.isAnnotationPresent(Phone.Get::class) -> true         
                    this@getCommonBody.isAnnotationPresent(Phone.Post::class) -> false         
                    else -> Args.defaultMethod == "get"
                }},
            )${insertIf(getAnnotationByType(Phone.Auth::class)?.withToken == true){
                """
                {
                    header(HttpHeaders.Authorization, token)
                }
                """.trim()
            }}
            
            checkNoBadRequest(response)
            """
            .let(::append)
        }

        append("}")
    }