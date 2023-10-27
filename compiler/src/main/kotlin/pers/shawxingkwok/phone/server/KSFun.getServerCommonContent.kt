package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Kind
import pers.shawxingkwok.phone.getMethodInfo
import pers.shawxingkwok.phone.pathEnd

context (CodeFormatter)
internal fun KSFunctionDeclaration.getServerCommonContent(
    ksclass: KSClassDeclaration,
    kind: Kind.Common,
): String{
    val (methodName, withForm) = getMethodInfo(ksclass)
    val start = methodName.replaceFirstChar { it.lowercase() }
    val returnType = kind.returnType

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

        val invokeText = buildString {
            append("${ksclass.apiPropNameInPhone}.${simpleName()}(")
            append(getServerParametersPart(ksclass, start))
            append(")()")
        }

        when{
            returnType == resolver.builtIns.unitType -> invokeText

            returnType.isMarkedNullable ->
                """
                val ret = $invokeText
                
                if(ret == null)
                    ~call.response.status(HttpStatusCode.NotFound)!~
                else{
                    val text = encode(ret, ${returnType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})
                    call.respondText(text, status = HttpStatusCode.OK)                            
                }
                """.trim()

            else -> {
                """
                val ret = $invokeText
                    
                val text = encode(ret, ${returnType.getSerializerText()}, ${getCipherTextForReturn(ksclass)})
                call.respondText(text, status = HttpStatusCode.OK)                            
                """.trim()
            }
        }
        .let(::append)

        append("\n}")
    }
}