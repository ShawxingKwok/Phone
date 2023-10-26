package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import pers.shawxingkwok.ksputil.*
import java.rmi.server.RMIServerSocketFactory

internal fun buildServerPhone() {
    createFile(
        phones = MyProcessor.phones,
        packageName = Args.ServerPackageName,
        initialImports = setOf(
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "kotlinx.serialization.json.Json",
            "kotlinx.serialization.encodeToString",
            "kotlinx.serialization.KSerializer",
            "kotlinx.serialization.builtins.ByteArraySerializer",
            "pers.shawxingkwok.phone.Phone",
        ),
    ){
        """
        ${insertIf(MyProcessor.hasWebSocket){
            """
            typealias WebSocketConnector = suspend ${Decls().DefaultWebSocketServerSession}.() -> Unit
            typealias WebSocketRawConnector = suspend ${Decls().WebSocketServerSession}.() -> Unit
            """
        }}
        typealias CommonConnector<T> = suspend ${Decls().PipelineContextUnitCall}.() -> T
        typealias FileConnector<T> = Pair<T, CommonConnector<Unit>>

        object Phone{
            ${MyProcessor.phones.joinToString("\n\n"){ ksclass ->
                """
                interface ${ksclass.apiNameInPhone} : ${ksclass.qualifiedName()}{
                    fun Route.doOtherTasks(){}
                    
                    ${ksclass.getNeededFunctions().joinToString("\n\n"){ ksfun ->
                        val webSocketAnnot = ksfun.getAnnotationByType(Phone.WebSocket::class)
                    
                        val returnedText = when {
                            ksfun.commonArgType != null -> "CommonConnector<${ksfun.commonArgType!!.text}>" 
                            ksfun.fileArgType != null -> "FileConnector<${ksfun.fileArgType}>" 
                            webSocketAnnot == null -> TODO("other features")
                            webSocketAnnot.isRaw -> "WebSocketRawConnector"
                            else -> "WebSocketConnector"
                        }
                        ksfun.getHeader(returnedText)  
                    }}
                }
                """
            }}
            
            ${getCoderFunctions()}
            
            ${insertIf(MyProcessor.hasWebSocket){
                """
                private suspend fun ${Decls().WebSocketServerSession}.unacceptedClose(text: String){
                    val closeReason = ${Decls().CloseReason}(${Decls().CloseReason}.Codes.CANNOT_ACCEPT, text)
                    ${Decls().close}(closeReason)
                }
                """                
            }}
    
            fun routeAll(
                route: Route,
                ${MyProcessor.phones.joinToString("\n"){ ksclass ->
                    "${ksclass.apiPropNameInPhone}: ${ksclass.apiNameInPhone},"
                }}    
            ){
                ${MyProcessor.phones.joinToString("\n"){ ksclass ->
                    "route(route, ${ksclass.apiPropNameInPhone})"
                }}
            }
            
            ${MyProcessor.phones.joinToString(""){ ksclass ->
                """
                fun route(route: Route, ${ksclass.apiPropNameInPhone}: ${ksclass.apiNameInPhone}){
                    route.route("/${ksclass.apiNameInPhone}"){
                        ${ksclass.apiPropNameInPhone}.run { doOtherTasks() }
                        
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
        val isWebSocket = isAnnotationPresent(Phone.WebSocket::class)
        val isWebSocketRaw = getAnnotationByType(Phone.WebSocket::class)?.isRaw == true
        val fileArgType = fileArgType
        val commonArgType = commonArgType

        val methodText = when {
            !isWebSocket -> getOrPost(ksclass)
            isWebSocketRaw -> Decls().serverWebSocketRaw
            else -> Decls().serverWebSocket
        }

        append("""
            $methodText("/$pathEnd"){
            """.trimStart()
        )

        when{
            parameters.none() -> {}

            isWebSocket
            || fileArgType != null
            || methodText == "get" ->
                append("val params = call.request.queryParameters\n\n")

            else -> append("val params = call.${Decls().receiveParameters}()\n\n")
        }

        when{
            commonArgType != null
            && commonArgType != resolver.builtIns.unitType ->
                append("val ret = ")

            fileArgType != null
            && fileArgType != resolver.builtIns.unitType ->
                append("val (headInfo, act) = ")
        }

        append("${ksclass.apiPropNameInPhone}.${simpleName()}(")

        if (parameters.any()) append("\n")

        parameters.forEach { param ->
            val paramName = param.name!!.asString()
            val type = param.type.resolve()

            if (ksclass.isAnnotationPresent(Phone.WebSocket::class)
                && type.declaration is KSTypeParameter
            )
                append("$paramName = this,")
            else {
                val paramTypeText =
                    if (param.isVararg) when (type) {
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
                        try{
                            decode<${paramTypeText.removeSuffix("?")}>(it, ${param.getSerializerText()}, ${param.getCipherText(ksclass)})
                        }catch(tr: Throwable){
                            ${run {  
                                val text = "The parameter `${paramName}` is incorrectly serialized.\\n${'$'}tr"  
                                if (isWebSocket)
                                    "unacceptedClose(\"$text\")"
                                else
                                    """
                                    call.respondText(
                                        text = "$text",
                                        status = HttpStatusCode.BadRequest
                                    )
                                    """.trim()
                            }}
                            return@$methodText
                        }
                    }!~
                    ${insertIf(!type.isMarkedNullable){
                        """
                        ~?: return@$methodText ${run{
                            val text = "Not found `${paramName}` in received parameters."
                            
                            if (isWebSocket)
                                "unacceptedClose(\"$text\")!~"
                            else
                                """
                                call.respondText(
                                    text = "$text",
                                    status = HttpStatusCode.BadRequest
                                )!~
                                """.trim()
                        }} 
                        """.trim()
                    }} 
                """.trim()
                    .let(::append)

                insert(length - 2, ",")
            }
            append("\n\n")
        }

        when{
            commonArgType != null || isWebSocket -> append(")()\n\n")
            fileArgType != null -> append(")\n\n")
            else -> TODO()
        }

        when {
            isWebSocket -> {}

            commonArgType != null ->
                if (commonArgType != resolver.builtIns.unitType)
                    """
                    ${insertIf(commonArgType.isMarkedNullable){
                        """
                        if(ret == null)
                            ~call.response.status(HttpStatusCode.NotFound)!~
                        else{
                        """
                    }}    
                        val text = encode(ret, ${commonArgType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})
                        call.respondText(text, status = HttpStatusCode.OK)
                    ${insertIf(commonArgType.isMarkedNullable){ "}" }}
                    """.trimStart()
                        .let(::append)

            fileArgType != null ->
                if (fileArgType != resolver.builtIns.unitType) {
                    if (fileArgType.isMarkedNullable)
                        append("if (headInfo != null) {\n")

                    append("""
                        val text = encode(headInfo, ${fileArgType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})
                        call.response.header("Phone-Info", text)
                        call.response.status(HttpStatusCode.OK)
                    """.trimStart())

                    if (fileArgType.isMarkedNullable)
                        append("}\n")

                    append("\nact()\n")
                }

            else -> TODO("other future features")
        }

        append("}")
    }
}