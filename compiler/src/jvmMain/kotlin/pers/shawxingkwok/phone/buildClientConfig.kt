@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildClientConfig(all: List<KSClassDeclaration>) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ClientPackageName,
        dependencies = Dependencies(true, *all.map{ it.containingFile!! }.toTypedArray()),
        fileName = "Calls",
        header = Suppressing,
        extensionName = "",
        additionalImports =
            listOf(
                "io.ktor.client.*",
                "io.ktor.client.request.*",
                "io.ktor.http.*",
                "io.ktor.client.statement.*",
                "kotlinx.serialization.encodeToString",
                "kotlinx.serialization.json.Json",
            )
    ) {
        """
        class Calls(private val client: HttpClient) {
            private companion object {
                const val BASIC_URL = "${Args.BasicUrl}"
            }            
            
            private fun HttpRequestBuilder.jsonParameter(key: String, value: Any?){
                val newV = encode(value ?: return)
                parameter(key, newV)
            }
            
            ${coder()}
                                
            ${all.joinToString("\n\n") { apiKSClass ->
                """
                val ${apiKSClass.simpleName().replaceFirstChar(Char::lowercase)} = object: ${apiKSClass.asStarProjectedType().text} {                    
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
private fun KSFunctionDeclaration.getText() = buildString{
    append("override suspend fun ${this@getText}(")

    if (parameters.size <= 2)
        parameters.joinToString(postfix = ")", separator = ", ") {
            "${it.name!!.asString()}: ${it.type.text}"
        }
        .let(::append)
    else
        parameters.joinToString(prefix = "\n", postfix = ")", separator = "") {
            "${it.name!!.asString()}: ${it.type.text},\n"
        }
        .let(::append)

    val hasReturn = returnType!!.resolve() != resolver.builtIns.unitType
    if (hasReturn)
        append(": ${returnType!!.text} {\n")
    else
        append(" {\n")

    append("val response = client.get(\"$${"mBasicUrl"}/${simpleName()}\")")

    if (parameters.any()) {
        append(" {\n")
        parameters.forEach {
            append("jsonParameter(\"${it.name!!.asString()}\", ${it.name!!.asString()})\n")
        }
        append("}")
    }
    append("\n")

    append("""
        check(response.status != HttpStatusCode.BadRequest){
            response.bodyAsText()
        }
    """.trim())
    append("\n")

    if (hasReturn){
        if (returnType!!.resolve().isMarkedNullable) {
            append("if(response.status != HttpStatusCode.NotFound)\n")
            append("~return null!~\n")
        }
        append("return response.bodyAsText().let(::decode)\n")
    }

    append("}")
}