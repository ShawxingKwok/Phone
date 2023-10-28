package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Kind
import pers.shawxingkwok.phone.getMethodInfo
import pers.shawxingkwok.phone.kind
import pers.shawxingkwok.phone.pathEnd
import pers.shawxingkwok.phone.server.parts.getServerParametersPart

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerRouteContent(ksclass: KSClassDeclaration): String {
    val retType = when(val kind = kind){
        is Kind.WebSocket -> return getServerWebSocketContent(ksclass, kind)
        is Kind.Manual -> kind.tagType
        is Kind.Common -> kind.returnType
        is Kind.PartialContent -> kind.tagType
    }

    val (methodName, withForm) = getMethodInfo(ksclass)
    val start = methodName.replaceFirstChar { it.lowercase() }

    return buildString {
        """
        $start("/$pathEnd"){
        """.trimStart()
            .let(::append)

        when{
            parameters.none() -> {}
            withForm -> append("val params = call.${Decls().receiveParameters}()\n\n")
            else -> append("val params = call.request.queryParameters\n\n")
        }

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

        if (kind is Kind.Common)
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
                kind is Kind.PartialContent -> append("val (ret, file) = $invokePart\n\n")
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

            if (kind is Kind.PartialContent) {
                append("call.response.status(HttpStatusCode.OK)\n")
                append("call.respondFile(file)\n")
            }else
                // Manual
                append("call.response.status(HttpStatusCode.OK)\n")
        }

        append("}")
    }
}