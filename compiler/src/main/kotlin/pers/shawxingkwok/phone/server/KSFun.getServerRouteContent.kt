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

        when{
            retType == resolver.builtIns.unitType -> "$invokePart\n"

            call is Call.Common && retType.isMarkedNullable ->
                """
                val ret = $invokePart                
                if (ret == null) 
                    ~call.response.status(HttpStatusCode.NoContent)!~
                else{                    
                    $`val text = encode~~`
                    call.respondText(text)                            
                }
                """

            call is Call.Common ->
                """
                val ret = $invokePart                
                $`val text = encode~~`
                call.respondText(text)           
                """

            retType.isMarkedNullable ->
                """
                val ret = $invokePart                
                if (ret != null){
                    $`val text = encode~~`
                    call.response.header("Phone-Tag", text)                                            
                }    
                """

            else ->
                """
                val ret = $invokePart                
                $`val text = encode~~`
                call.response.header("Phone-Tag", text)          
                """
        }
        .trimStart().let(::append)

        append("call.response.status(HttpStatusCode.OK)\n")
        append("}")
    }
}