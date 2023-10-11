package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildServerPhone(phones: List<KSClassDeclaration>) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ServerPackageName,
        fileName = "Phone",
        dependencies = Dependencies(true, *phones.map{ it.containingFile!! }.toTypedArray()),
        header = Suppressing,
        extensionName = "",
        initialImports = listOf(
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "kotlinx.serialization.json.Json",
            "kotlinx.serialization.encodeToString",
            "kotlinx.serialization.KSerializer",
            "kotlinx.serialization.SerializationStrategy",
            "kotlinx.serialization.DeserializationStrategy",
        ),
    ){
        """
        object Phone{
            ${phones.joinToString("\n"){ 
                "abstract class ${it.simpleName()}(protected val call: ApplicationCall) : ${it.qualifiedName()}" 
            }}
            
            ${getCoderFunctions()}

            fun configure(
                routing: Routing,
                ${phones.joinToString("\n"){
                    "get${it.simpleName()}: (ApplicationCall) -> ${it.simpleName()},"   
                }}    
            ){
                ${phones.joinToString("\n\n"){ ksclass ->
                    """
                    routing.route("${ksclass.simpleName()}"){
                        ${ksclass.getNeededFunctions().joinToString("\n\n"){ it.getText() }}
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

    if (returnType!!.resolve() != resolver.builtIns.unitType)
        append("val ret = ")

    append("get${parentDeclaration!!.simpleName()}(call).${simpleName()}(\n")

    parameters.forEach { param ->
        val paramName = param.name!!.asString()
        val type = param.type.resolve()

        append("$paramName = params[\"$paramName\"]")
        if (type.declaration.qualifiedName() != "kotlin.String")
            append(
                """?.let{
                    ~try {
                        decode(it, ${param.getSerializer()?.text})
                    }catch (_: Throwable){
                        val text = "The parameter $paramName is incorrectly serialized."
                        call.respondText(text, status = HttpStatusCode.BadRequest)
                        return@get
                    }
                }!~
                """.trim()
            )

        if (!type.isMarkedNullable)
            append(
                """
                ~?: return@get call.respondText(
                    text = "Not found `$paramName` in parameters.",
                    status = HttpStatusCode.BadRequest,
                ),!~
               """
            )
        else
            append(",\n")
    }

    if (parameters.none())
        insert(length - 1, ")")
    else
        append(")\n")

    when (val returnType = returnType!!.resolve()) {
        resolver.builtIns.unitType ->
            append("call.response.status(HttpStatusCode.OK)\n")
        else -> {
            mayEmbrace(
                condition = returnType.isMarkedNullable,
                start = """
                    if(ret == null)
                        ~call.response.status(HttpStatusCode.NotFound)!~
                    else{
                    """.trimStart(),
                end =  "}\n",
            ){
                """
                val text = encode(ret, ${MyProcessor.serializers[returnType]?.text})
                call.respondText(text, status = HttpStatusCode.OK)
                """.trimStart()
            }
            .let(::append)
        }
    }

    append("}")
}