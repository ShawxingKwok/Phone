@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildClientPhone(phoneApis: List<KSClassDeclaration>) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ClientPackageName,
        dependencies = Dependencies(true, *phoneApis.map{ it.containingFile!! }.toTypedArray()),
        fileName = "Phone",
        extensionName = "",
        initialImports =
            listOf(
                "io.ktor.client.*",
                "io.ktor.client.request.*",
                "io.ktor.http.*",
                "io.ktor.client.statement.*",
                "kotlinx.serialization.encodeToString",
                "kotlinx.serialization.json.Json",
                "kotlinx.serialization.KSerializer",
                "kotlinx.serialization.builtins.ByteArraySerializer",
                "pers.shawxingkwok.phone.Phone",
            )
    ) {
        """
        class Phone(private val client: HttpClient) {
            private companion object {
                const val BASIC_URL = "${Args.BasicUrl}"
            }            
            
            private inline fun <reified T> HttpRequestBuilder.jsonParameter(
                key: String,
                value: T,
                serializer: KSerializer<T & Any>?,
                cipher: Phone.Cipher?,
            ){
                if (value == null) return
                val newV = encode(value, serializer, cipher)
                parameter(key, newV)
            }
            
            private suspend fun checkNoBadRequest(response: HttpResponse){
                check(response.status != HttpStatusCode.BadRequest){
                    response.bodyAsText()
                }
            }
            
            ${getCoderFunctions()}
            
            ${phoneApis.joinToString("\n\n") { apiKSClass ->
                """
                val ${apiKSClass.simpleName().replaceFirstChar(Char::lowercase)} = object : ${apiKSClass.asStarProjectedType().text} {                    
                    private val mBasicUrl = "${"$"}{BASIC_URL}/${apiKSClass.simpleName()}" 
                
                    ${apiKSClass.getNeededFunctions().joinToString("\n\n"){ it.getText() }}
                }
                """.trim()
            }}    
        }
        """.trim().indentAsKtCode()
    }
}

context (KtGen)
private fun KSFunctionDeclaration.getText() =
    buildString {
        append("override suspend fun ${this@getText}(")

        if (parameters.size <= 2)
            parameters.joinToString(postfix = ")", separator = ", ") {
                "${insertIf(it.isVararg){ "vararg " }}${it.name!!.asString()}: ${it.type.text}"
            }
            .let(::append)
        else
            parameters.joinToString(prefix = "\n", postfix = ",\n)", separator = ",\n") {
                "${insertIf(it.isVararg){ "vararg " }}${it.name!!.asString()}: ${it.type.text}"
            }
            .let(::append)

        mayEmbrace(
            condition = returnType!!.resolve() != resolver.builtIns.unitType,
            getStart = {
                append(": ${returnType!!.text}")
            },
            getEnd = {
                val returnType = returnType!!.resolve()

                if (returnType.isMarkedNullable) {
                    append("if(response.status != HttpStatusCode.NotFound)\n")
                    append("~return null!~\n\n")
                }
                append("return decode(response.bodyAsText(), ${MyProcessor.serializers[returnType]?.text}, ${this@getText.getCipherTextForReturn()})\n")
            },
        ) {
            append(" {\n")
            append("val response = client.post(\"$${"mBasicUrl"}/${simpleName()}\")")

            if (parameters.any()) {
                append(" {\n")
                parameters.forEach { ksParam ->
                    append("jsonParameter(\"${ksParam.name!!.asString()}\", ")
                    append("${ksParam.name!!.asString()}, ")
                    append("${ksParam.getSerializer()?.text}, ")
                    append("${ksParam.getCipherText()})\n")
                }
                append("}")
            }

            append("\n\ncheckNoBadRequest(response)\n\n")
        }

        append("}")
    }