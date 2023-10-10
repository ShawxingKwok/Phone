@file:Suppress("LocalVariableName")

package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.*

internal fun buildClientConfig(
    phones: List<KSClassDeclaration>,
    serializers: Map<KSType, KSClassDeclaration>,
) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ClientPackageName,
        dependencies = Dependencies(true, *phones.map{ it.containingFile!! }.toTypedArray()),
        fileName = "Phone",
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
                "kotlinx.serialization.KSerializer",
                "kotlinx.serialization.SerializationStrategy",
                "kotlinx.serialization.DeserializationStrategy",
            )
    ) {
        """
        class Phone(private val client: HttpClient) {
            private companion object {
                const val BASIC_URL = "${Args.BasicUrl}"
            }            
            
            @Suppress("UNCHECKED_CAST")
            private fun HttpRequestBuilder.jsonParameter(
                key: String,
                value: Any?,
                serializer: KSerializer<out Any>?
            ){
                if (value == null) return
                val newV = encode(value, serializer as KSerializer<Any>?)
                parameter(key, newV)
            }
        
            ${getCoderFunctions()}
            
            ${phones.joinToString("\n\n") { apiKSClass ->
                """
                val ${apiKSClass.simpleName().replaceFirstChar(Char::lowercase)} = object : ${apiKSClass.asStarProjectedType().text} {                    
                    private val mBasicUrl = "${"$"}{BASIC_URL}/${apiKSClass.simpleName()}" 
                
                    ${apiKSClass.getNeededFunctions().joinToString("\n\n"){ it.getText(serializers) }}
                }
                """.trim()
            }}    
        }
        """.trim().indentAsKtCode()
    }
}

context (KtGen)
private fun KSFunctionDeclaration.getText(serializers: Map<KSType, KSClassDeclaration>)
=
    buildString {
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
                val type = it.type.resolve()
                val serializer = serializers[type]
                append("jsonParameter(\"${it.name!!.asString()}\", ${it.name!!.asString()}, ${serializer?.text})\n")
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

        if (hasReturn) {
            val returnType = returnType!!.resolve()

            if (returnType.isMarkedNullable) {
                append("if(response.status != HttpStatusCode.NotFound)\n")
                append("~return null!~\n")
            }

            append("val text = response.bodyAsText()\n")

            when(val serializer = serializers[returnType]){
                null -> append("return decode(text, null)\n")
                else -> append("return decode(text, ${serializer.text})\n")
            }
        }

        append("}")
    }