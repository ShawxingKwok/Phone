package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildServerConfig(all: List<KSClassDeclaration>){
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ServerPackageName,
        fileName = "Phone",
        dependencies = Dependencies(true, *all.map{ it.containingFile!! }.toTypedArray()),
        header = Suppressing,
        extensionName = "",
        additionalImports = listOf(
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "kotlinx.serialization.json.Json",
            "kotlinx.serialization.encodeToString",
        ),
    ){
        """
        object Phone{
            ${all.joinToString("\n"){ "abstract class ${it.simpleName()}(val call: ApplicationCall) : ${it.qualifiedName()}" }}
            
            ${coder()}
            
            fun configure(
                routing: Routing,
                ${all.joinToString("\n"){
                    "get${it.simpleName()}: (ApplicationCall) -> ${it.simpleName()},"   
                }}    
            ){
                ${all.joinToString("\n\n"){ ksclassDecl ->
                    """
                    routing.route("${ksclassDecl.simpleName()}"){
                        ${ksclassDecl.getNeededFunctions().joinToString("\n\n"){ it.getText() }}
                    }
                    """.trim()
                }}
            }            
        }
        """.trim().indentAsKtCode()
    }
}

context (KtGen)
private fun KSFunctionDeclaration.getText() = buildString{
    append("get(\"/${simpleName()}\"){\n")

    if (parameters.any())
        append("val params = call.request.queryParameters\n\n")

    parameters.forEach { param ->
        val paramName = param.name!!.asString()
        val type = param.type.resolve()

        append("val _$paramName: ${type.text} = params[\"$paramName\"]\n")

        val isString = type.declaration.qualifiedName() == "kotlin.String"
        if (isString || !type.isMarkedNullable)
            append("~")

        if (!isString) {
            append("""
                ?.let{
                    try {
                        decode(it)
                    }catch (_: Throwable){
                        val text = "The $paramName is incorrectly serialized."
                        call.respondText(text, status = HttpStatusCode.BadRequest)
                        return@get
                    }
                }
            """.trim())
            append("\n")
        }

        if (!type.isMarkedNullable) {
            append("""
                ?: return@get call.respondText(
                    text = "Not found $paramName in parameters.",
                    status = HttpStatusCode.BadRequest,
                )
            """.trim())
            append("\n")
        }

        if (isString || !type.isMarkedNullable)
            insert(length, "!~")

        append("\n")
    }

    val commandText = "get${parentDeclaration!!.simpleName()}(call).${simpleName()}(${parameters.joinToString(", "){ "_" + it.name!!.asString() }})"

    when{
        returnType!!.resolve() == resolver.builtIns.unitType ->
            """
            $commandText
            call.response.status(HttpStatusCode.OK)                
            """

        returnType!!.resolve().isMarkedNullable ->
            """
            when(val ret = $commandText){
                null -> call.response.status(HttpStatusCode.NotFound)
                else -> call.respondText(encode(ret), status = HttpStatusCode.OK)
            }
            """

        else -> """
            val ret = $commandText
            call.respondText(encode(ret), status = HttpStatusCode.OK)
        """
    }
    .trim()
    .let(::append)

    append("\n}")
}