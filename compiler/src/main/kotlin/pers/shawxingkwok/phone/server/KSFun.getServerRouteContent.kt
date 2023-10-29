package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Call
import pers.shawxingkwok.phone.getCall
import pers.shawxingkwok.phone.getPathEnd
import pers.shawxingkwok.phone.server.parts.getServerParametersPart

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerRouteContent(ksclass: KSClassDeclaration): String {
    val call = getCall(ksclass)

    val retType = when(call){
        is Call.WebSocket -> return getServerWebSocketContent(ksclass, call)
        is Call.Manual -> call.tagType
        is Call.Common -> call.returnType
        is Call.PartialContent -> call.tagType
    }

    val start = call.method.routeName

    return buildString {
        """
        $start("/${getPathEnd(ksclass)}"){
        """.trimStart()
            .let(::append)

        if (parameters.any())
             """
             val params = call.request.queryParameters
                ~.takeUnless { it.isEmpty() } 
                ?: call.${Decls().receiveParameters}()!~                 
                
             """.trimStart()
            .let(::append)

        val invokePart = buildString {
            append("${ksclass.apiPropNameInPhone}.${simpleName()}(")
            append(getServerParametersPart(ksclass, start))
            append(")()")
        }

        @Suppress("LocalVariableName")
        val `val text = encode~~` by fastLazy {
            "val text = encode(ret, ${retType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})"
        }

        val isUnit = retType == resolver.builtIns.unitType

        if (call is Call.Common)
            when{
                isUnit ->
                    """
                    $invokePart
                    call.response.status(HttpStatusCode.OK)
                    """

                retType.isMarkedNullable ->
                    """
                    val ret = $invokePart
                    
                    if (ret == null) 
                        ~call.response.status(HttpStatusCode.NoContent)!~
                    else{                    
                        $`val text = encode~~`
                        call.respondText(text)                            
                        call.response.status(HttpStatusCode.OK)    
                    }
                    """

                else ->
                    """
                    val ret = $invokePart
    
                    $`val text = encode~~`
                    call.respondText(text)           
                    call.response.status(HttpStatusCode.OK)    
                    """
            }
            .trimStart().let(::append)
        else {
            when{
                call is Call.PartialContent -> append("val (ret, file) = $invokePart\n\n")
                // Manual
                isUnit -> append("$invokePart\n\n")
                else -> append("val ret = $invokePart\n\n")
            }

            when{
                isUnit -> {}

                retType.isMarkedNullable -> {
                    """                        
                    if (ret != null){
                        $`val text = encode~~`
                        call.response.header("Phone-Tag", text)                                            
                    }
                    """.trimStart()
                        .let(::append)
                }

                else ->
                    """
                    $`val text = encode~~`
                    call.response.header("Phone-Tag", text)                        
                    """.trimStart()
                        .let(::append)
                }

            if (call is Call.PartialContent) {
                append("call.response.status(HttpStatusCode.OK)\n")
                append("call.respondFile(file)\n")
            }else
                // Manual
                append("call.response.status(HttpStatusCode.OK)\n")
        }

        append("}")
    }
}