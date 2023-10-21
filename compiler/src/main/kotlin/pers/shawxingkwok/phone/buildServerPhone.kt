package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildServerPhone(phones: List<KSClassDeclaration>) {
    createFile(
        phones = phones,
        packageName = Args.ServerPackageName,
        initialImports = setOf(
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "io.ktor.server.request.*",
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
                interface ${ksclass.phoneName} : ${ksclass.qualifiedName()} {
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
                }catch (tr: Throwable){
                    val msg = "The parameter `${"$"}paramName` is incorrectly serialized.\n${'$'}tr"
                    call.respondText(msg, status = HttpStatusCode.BadRequest)
                    null
                }!~
            
            private suspend fun notFoundParam(call: ApplicationCall, paramName: String){
                call.respondText(
                    text = "Not found `${"$"}paramName` in parameters.",
                    status = HttpStatusCode.BadRequest,
                )
            }
    
            fun route(
                route: Route,
                ${phones.joinToString("\n"){
                    "get${it.phoneName}: (${it.getPropTypeText(true)}) -> ${it.phoneName},"   
                }}    
            ){
                ${phones.joinToString(""){ ksclass ->
                    """
                    route.route("/${ksclass.phoneName}"){
                        ${mayEmbraceWithAuth(ksclass) {
                            ksclass.getNeededFunctions().joinToString("\n\n") { it.getBody(ksclass) }
                        }}
                    }
                    """
                }}
            }
        }
        """
    }
}

context (CodeFormatter)
private fun KSFunctionDeclaration.getBody(ksclass: KSClassDeclaration) = mayEmbraceWithAuth(this) {
    buildString {
        val webSocketAnnot = ksclass.getAnnotationByType(Phone.WebSocket::class)

        val methodText = when(val method = getMethod(ksclass)){
            Method.GET, Method.POST -> method.text
            else -> getDeclText(
                import = "io.ktor.server.websocket.webSocket${insertIf(method == Method.WEB_SOCKET_RAW) { "Raw" }}",
                innerName = null,
                isTopLevelAndExtensional = true
            )
        }

        append("""$methodText("/${simpleName()}$mayPolymorphicId"""")

        if (webSocketAnnot?.subProtocol?.any() == true)
            append(""", "${webSocketAnnot.subProtocol}"""")

        append("){\n")

        if (parameters.any()) {
            if (webSocketAnnot == null)
                append("val params = call.receiveParameters()\n\n")
            else
                append("val params = call.request.queryParameters\n\n")
        }

        val returnType = returnType!!.resolve()
        if (returnType != resolver.builtIns.unitType)
            append("val ret = ")

        append("get${ksclass.phoneName}(")

        if (webSocketAnnot == null)
            append("call")
        else
            append("this")

        append(").${simpleName()}(")

        if (parameters.any()) append("\n")

        parameters.forEach { param ->
            val paramName = param.name!!.asString()
            val type = param.type.resolve()

            val typeText =
                if (param.isVararg)
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
                else
                    type.text

            """
            $paramName = params["$paramName"]
                ~?.let{ 
                    tryDecode${"<${typeText}>"}(call, it, "$paramName", ${param.getSerializerText()}, ${param.getCipherText(ksclass)}) 
                    ?: return@$methodText 
                }!~
                ${insertIf(!type.isMarkedNullable){
                    "~?: return@$methodText notFoundParam(call, \"$paramName\")!~\n"
                }}                
            """.trim().let(::append)

            insert(length - 2, ",")
            append("\n\n")
        }

        append(")\n\n")

        when {
            ksclass.isAnnotationPresent(Phone.WebSocket::class) -> {}

            returnType == resolver.builtIns.unitType ->
                append("call.response.status(HttpStatusCode.OK)\n")

            else ->
                """
                ${insertIf(returnType.isMarkedNullable){
                    """
                    if(ret == null)
                        ~call.response.status(HttpStatusCode.NotFound)!~
                    else{
                    """
                }}    
                    val text = encode(ret, ${returnType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})
                    call.respondText(text, status = HttpStatusCode.OK)
                ${insertIf(returnType.isMarkedNullable){ "}" }}
                """.trimStart()
                    .let(::append)
        }
        append("}")
    }
}