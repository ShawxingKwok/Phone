package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildServerPhone(phones: List<KSClassDeclaration>) {
    Environment.codeGenerator.createFileWithKtGen(
        packageName = Args.ServerPackageName,
        fileName = "Phone",
        dependencies = Dependencies(true, *phones.map{ it.containingFile!! }.toTypedArray()),
        extensionName = "",
        initialImports = setOf(
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
            ${phones.joinToString("\n"){ ksclass ->
            """
            interface ${ksclass.simpleName()} : ${ksclass.qualifiedName()} {
                ${ksclass.getPropStatement(true)}
            }
            """.trim()
            }}
            
            ${getCoderFunctions()}

            private suspend inline fun <reified T: Any> tryDecode(
                call: ApplicationCall,
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
            
            private suspend fun notFoundParam(call: ApplicationCall, paramName: String){
                call.respondText(
                    text = "Not found `${"$"}paramName` in parameters.",
                    status = HttpStatusCode.BadRequest,
                )
            }
    
            fun configure(
                routing: Routing,
                ${phones.joinToString("\n"){
                    "get${it.simpleName()}: (${it.getPropTypeText(true)}) -> ${it.simpleName()},"   
                }}    
            ){
                ${phones.joinToString("\n\n"){ ksclass ->
                    """
                    routing.route("/${ksclass.simpleName()}"){
                        ${mayEmbraceWithAuth(ksclass) {
                            ksclass.getNeededFunctions().joinToString("\n\n") { it.getBody(ksclass) }
                        }}
                    }
                    """.trim()
                }}
            }
        }
        """
    }
}

context (KtGen)
private fun KSFunctionDeclaration.getBody(ksclass: KSClassDeclaration) = mayEmbraceWithAuth(this) {
    buildString {
        val websocketsAnnot = ksclass.getAnnotationByType(Phone.WebSocket::class)

        val postOrWebSocket = when{
            websocketsAnnot == null -> "post"
            else -> getDeclText(
                import = "io.ktor.server.websocket.webSocket${insertIf(websocketsAnnot.isRaw) { "Raw" }}",
                innerName = null,
                isTopLevelAndExtensional = true
            )
        }

        append("""$postOrWebSocket("/$mayPolymorphicPath"""")

        if (websocketsAnnot?.protocol != null)
            append(""", "${websocketsAnnot.protocol}"""")

        append("){\n")

        if (parameters.any())
            append("val params = call.request.queryParameters\n\n")

        val returnType = returnType!!.resolve()
        if (returnType != resolver.builtIns.unitType)
            append("val ret = ")

        append("get${parentDeclaration!!.simpleName()}(")

        if (websocketsAnnot == null)
            append("call")
        else
            append("this")

        append(").${simpleName()}(\n")

        parameters.forEach { param ->
            val paramName = param.name!!.asString()
            val type = param.type.resolve()

            append("$paramName = params[\"$paramName\"]\n")

            val typeText =
                if (param.isVararg) {
                    when (type) {
                        resolver.builtIns.booleanType -> BooleanArray::class.text
                        resolver.builtIns.charType -> CharArray::class.text

                        resolver.builtIns.byteType -> ByteArray::class.text
                        resolver.builtIns.shortType -> ShortArray::class.text
                        resolver.builtIns.intType -> IntArray::class.text
                        resolver.builtIns.longType -> LongArray::class.text

                        resolver.builtIns.floatType -> FloatArray::class.text
                        resolver.builtIns.doubleType -> DoubleArray::class.text

                        else -> Array::class.text + "<out ${type.text}>"
                    }
                } else
                    type.text

            append(
                """
                ~?.let{ 
                    tryDecode${"<${typeText}>"}(call, it, "$paramName", ${param.getSerializer()?.text}, ${
                        param.getCipherText(ksclass)
                    }) 
                    ?: return@$postOrWebSocket 
                }!~
                """.trim() + "\n"
            )

            if (!type.isMarkedNullable)
                append("~?: return@$postOrWebSocket notFoundParam(call, \"$paramName\")!~\n")

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

        when {
            ksclass.isAnnotationPresent(Phone.WebSocket::class) -> {}

            returnType == resolver.builtIns.unitType ->
                append("\ncall.response.status(HttpStatusCode.OK)\n")

            else -> {
                append("\n")

                mayEmbrace(
                    condition = returnType.isMarkedNullable,
                    start = """
                        if(ret == null)
                            ~call.response.status(HttpStatusCode.NotFound)!~
                        else{
                        """.trimStart(),
                    body = """
                        val text = encode(ret, ${MyProcessor.serializers[returnType]?.text}, ${
                            this@getBody.getCipherTextForReturn(ksclass)
                        })
                        call.respondText(text, status = HttpStatusCode.OK)
                    """.trimStart(),
                    end = "}\n",
                )
                .let(::append)
            }
        }
        append("}")
    }
}