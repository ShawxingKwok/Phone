package pers.shawxingkwok.phone

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.*

internal fun buildServerConfig(
    phones: List<KSClassDeclaration>,
    serializers: Map<KSType, KSClassDeclaration>,
) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ServerPackageName,
        fileName = "Phone",
        dependencies = Dependencies(true, *phones.map{ it.containingFile!! }.toTypedArray()),
        header = Suppressing,
        extensionName = "",
        additionalImports = listOf(
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
            ${phones.joinToString("\n"){ "abstract class ${it.simpleName()}(protected val call: ApplicationCall) : ${it.qualifiedName()}" }}
            
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
                        ${ksclass.getNeededFunctions().joinToString("\n\n"){ it.getText(serializers) }}
                    }
                    """.trim()
                }}
            }
        }
        """.trim().indentAsKtCode()
    }
}

context (KtGen)
private fun KSFunctionDeclaration.getText(serializers: Map<KSType, KSClassDeclaration>) = buildString{
    append("get(\"/${simpleName()}\"){\n")

    if (parameters.any())
        append("val params = call.request.queryParameters\n\n")

    parameters.forEach { param ->
        val paramName = param.name!!.asString()
        val type = param.type.resolve()

        append("val _$paramName: ${type.text} = params[\"$paramName\"]\n")

        val isString = type.declaration.qualifiedName() == "kotlin.String"

        if (!isString)
            append(
                """
                ~?.let{
                    try {
                        decode(it, ${serializers[type]?.text})
                    }catch (_: Throwable){
                        val text = "The $paramName is incorrectly serialized."
                        call.respondText(text, status = HttpStatusCode.BadRequest)
                        return@get
                    }
                }!~
                """.trimStart()
            )

        if (!type.isMarkedNullable)
            append(
                """
                ~?: return@get call.respondText(
                    text = "Not found $paramName in parameters.",
                    status = HttpStatusCode.BadRequest,
                )!~
               """.trimStart()
            )

        append("\n")
    }

    val commandText = "get${parentDeclaration!!.simpleName()}(call).${simpleName()}(${parameters.joinToString(", "){ "_" + it.name!!.asString() }})"

    when (val returnType = returnType!!.resolve()) {
        resolver.builtIns.unitType ->
            """
            $commandText
            call.response.status(HttpStatusCode.OK)                
            """
            .trimStart()
        else -> {
            append("val ret = $commandText\n")

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
                val text = encode(ret, ${serializers[returnType]?.text})
                call.respondText(text, status = HttpStatusCode.OK)
                """.trimStart()
            }
        }
    }
    .let(::append)

    append("}")
}