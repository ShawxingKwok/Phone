package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.*

internal fun buildServerPhone() {
    createFile(
        phones = MyProcessor.phones,
        packageName = Args.ServerPackageName,
        initialImports = setOf(
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "io.ktor.server.request.*",
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
            ${MyProcessor.phones.joinToString("\n"){ ksclass ->
                """
                interface ${ksclass.implName} : ${ksclass.qualifiedName()} {
                    ${ksclass.getInterfacePropStatement()}
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
    
            fun routeAll(
                route: Route,
                ${MyProcessor.phones.joinToString("\n"){
                    "get${it.implName}: (${it.getInterfacePropTypeText()}) -> ${it.implName},"   
                }}    
            ){
                ${MyProcessor.phones.joinToString("\n"){
                    "route(route, get${it.implName})"
                }}
            }
            
            ${MyProcessor.phones.joinToString(""){ ksclass ->
                """
                @JvmName("${ksclass.implName}")
                fun route(
                    route: Route, 
                    get${ksclass.implName}: (${ksclass.getInterfacePropTypeText()}) -> ${ksclass.implName},   
                ){
                    route.route("/${ksclass.implName}"){
                        ${mayEmbraceWithAuth(ksclass) {
                            ksclass.getNeededFunctions().joinToString("\n\n") {
                                it.getBody(ksclass)
                            }
                        }}
                    }
                }
                """
            }}
        }
        """
    }
}

context (CodeFormatter)
private fun KSFunctionDeclaration.getBody(ksclass: KSClassDeclaration) = mayEmbraceWithAuth(this) {
    buildString {
        val methodText = when(val method = getMethod(ksclass)){
            Method.GET, Method.POST -> method.text
            else -> getDeclText(
                import = TODO(),
                innerName = null,
                isTopLevelAndExtensional = true
            )
        }

        append("""$methodText("/${simpleName()}$mayPolymorphicId"""")

        append("){\n")

        if (parameters.any()) {
            when(getMethod(ksclass)){
                Method.GET -> append("val params = call.request.queryParameters\n\n")
                Method.POST -> append("val params = call.receiveParameters\n\n")
                else -> TODO()
            }
        }

        val returnType = returnType!!.resolve()
        if (returnType != resolver.builtIns.unitType)
            append("val ret = ")

        append("get${ksclass.implName}(this)")

        append(".${simpleName()}(")

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
                    tryDecode${"<${typeText.removeSuffix("?")}>"}(call, it, "$paramName", ${param.getSerializerText()}, ${param.getCipherText(ksclass)}) 
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