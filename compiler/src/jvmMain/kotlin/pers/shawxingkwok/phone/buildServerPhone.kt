package pers.shawxingkwok.phone

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildServerPhone(phoneApis: List<KSClassDeclaration>) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ServerPackageName,
        fileName = "Phone",
        dependencies = Dependencies(true, *phoneApis.map{ it.containingFile!! }.toTypedArray()),
        header = Suppressing,
        extensionName = "",
        initialImports = listOf(
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "io.ktor.util.pipeline.*",
            "kotlinx.serialization.json.Json",
            "kotlinx.serialization.encodeToString",
            "kotlinx.serialization.KSerializer",
            "kotlinx.serialization.builtins.ByteArraySerializer",
            "pers.shawxingkwok.phone.Phone",
        ),
    ){
        """
        object Phone{
            ${phoneApis.joinToString("\n"){ 
                "abstract class ${it.simpleName()}(protected val call: ApplicationCall) : ${it.qualifiedName()}" 
            }}
            
            ${getCoderFunctions()}

            private suspend inline fun <reified T: Any> PipelineContext<Unit, ApplicationCall>.tryDecode(
                text: String,
                paramName: String,
                serializer: KSerializer<T>?,
                cipher: Phone.Cipher?,
            ): T? =
                ~try {
                    decode(text, serializer, cipher)
                }catch (_: Throwable){
                    val msg = "The parameter `${"$"}paramName` is incorrectly serialized."
                    call.respondText(msg, status = HttpStatusCode.BadRequest)
                    null
                }!~
            
            private suspend fun PipelineContext<Unit, ApplicationCall>.notFoundParam(paramName: String){
                call.respondText(
                    text = "Not found `${"$"}paramName` in parameters.",
                    status = HttpStatusCode.BadRequest,
                )
            }
    
            fun configure(
                routing: Routing,
                ${phoneApis.joinToString("\n"){
                    "get${it.simpleName()}: (ApplicationCall) -> ${it.simpleName()},"   
                }}    
            ){
                ${phoneApis.joinToString("\n\n"){ ksclass ->
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
    append("post(\"/${simpleName()}\"){\n")

    if (parameters.any())
        append("val params = call.request.queryParameters\n\n")

    val returnType = returnType!!.resolve()
    if (returnType != resolver.builtIns.unitType)
        append("val ret = ")

    append("get${parentDeclaration!!.simpleName()}(call).${simpleName()}(\n")

    parameters.forEach { param ->
        val paramName = param.name!!.asString()
        val type = param.type.resolve()

        append("$paramName = params[\"$paramName\"]\n")

        append("""
            ~?.let{ 
                tryDecode(it, "$paramName", ${param.getSerializer()?.text}, ${param.getCipherText()}) 
                ?: return@post 
            }!~
            """.trim() + "\n"
        )

        if (!type.isMarkedNullable)
            append("~?: return@post notFoundParam(\"$paramName\")!~\n")

        if (get(lastIndex - 1) == '~')
            insert(length - 3, ",")
        else
            insert(length - 1, ",")

        if (param != parameters.last())
            append("\n")
    }

    if (parameters.none())
        insert(length - 1, ")")
    else
        append(")\n")

    if (returnType == resolver.builtIns.unitType)
        append("call.response.status(HttpStatusCode.OK)\n")
    else
        mayEmbrace(
            condition = returnType.isMarkedNullable,
            start = """
                if(ret == null)
                    ~call.response.status(HttpStatusCode.NotFound)!~
                else{
                """.trimStart(),
            body = """
                val text = encode(ret, ${MyProcessor.serializers[returnType]?.text}, ${this@getText.getCipherTextForReturn()})
                call.respondText(text, status = HttpStatusCode.OK)
                """.trimStart(),
            end =  "}\n",
        )
        .let(::append)

    append("}")
}