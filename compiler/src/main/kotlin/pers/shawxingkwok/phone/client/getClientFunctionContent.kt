package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.Kind
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.client.parts.getClientHeader
import pers.shawxingkwok.phone.client.parts.getClientRequestPart
import pers.shawxingkwok.phone.client.parts.getClientTagStatement
import pers.shawxingkwok.phone.pathEnd
import pers.shawxingkwok.phone.*

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientFunctionContent(ksclass: KSClassDeclaration): String {
    val pathWithoutBasicUrl = "${ksclass.apiNameInPhone}/$pathEnd"
    val fullPath = "\$basicUrl/$pathWithoutBasicUrl"

    val requestPart = getClientRequestPart(ksclass)

    val core = when(val kind = kind) {
        is Kind.Common ->
            """
            val response = client.request("$fullPath"){
                $requestPart
            }
                
            response.check()
                
            ${insertIf(kind.returnType.isMarkedNullable) {
                """
                if(response.status == HttpStatusCode.NoContent)
                    ~return@runCatching null!~
                """.trim()
                }}

            ${insertIf(kind.returnType != resolver.builtIns.unitType) {
                val serializerText = kind.returnType.getSerializerText()
                "decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})"
            }}
            """

        is Kind.Manual ->
            """
            val response = client.request("$fullPath") {
                ${getClientRequestPart(ksclass)}
            }

            response.check()

            ${if (kind.tagType == resolver.builtIns.unitType)
                "response"
            else
                """
                ${getClientTagStatement(ksclass, kind.tagType)}
                tag to response                        
                """.trim()
            }
            """

        is Kind.PartialContent ->
            """
            val response = client.request("$fullPath") {
                ${getClientRequestPart(ksclass, MethodInfo("Head", false))}
            }
            
            response.check()
            
            ${if (kind.tagType == resolver.builtIns.unitType)
                "val tag = Unit"
            else
                getClientTagStatement(ksclass, kind.tagType)
            }            
            
            PartialContentHandler(tag) { ranges ->
                client.request("$fullPath"){
                    ${getClientRequestPart(ksclass)}
                    
                    if (ranges.none()) return@request

                    val v = ranges.joinToString(prefix = "bytes=", separator = ","){ range ->
                        "${'$'}{range.first}-${'$'}{range.last}"
                    }
                    header(HttpHeaders.Range, v)
                }
            }
            """

        is Kind.WebSocket ->{
            val sessionFunText =
                if (kind.isRaw)
                    Decls().clientWebSocketRawSession
                else
                    Decls().clientWebSocketSession

            """
            client.$sessionFunText(
                host = host, port = port,
                path = "$pathWithoutBasicUrl",
            ){
                enableWssIfNeeded(${kind.isRaw})
                ${getClientRequestPart(ksclass)}
            }
            """
        }
    }

    return """
        ${getClientHeader(ksclass)} =
            ~runCatching {
                $core
            }!~
        """.trim()
}