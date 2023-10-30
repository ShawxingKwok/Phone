package pers.shawxingkwok.phone.client

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.phone.Call
import pers.shawxingkwok.phone.apiNameInPhone
import pers.shawxingkwok.phone.client.parts.getClientHeader
import pers.shawxingkwok.phone.client.parts.getClientRequestPart
import pers.shawxingkwok.phone.client.parts.getClientTagStatement
import pers.shawxingkwok.phone.getPathEnd
import pers.shawxingkwok.phone.*

context (CodeFormatter)
internal fun KSFunctionDeclaration.getClientFunctionContent(ksclass: KSClassDeclaration): String {
    val pathWithoutBaseUrl = "${ksclass.apiNameInPhone}/${getPathEnd(ksclass)}"
    val fullPath = "\$baseUrl/$pathWithoutBaseUrl"

    val core = when(val call = getCall(ksclass)) {
        is Call.Common ->
            """
            val response = client.request("$fullPath"){
                ${getClientRequestPart(ksclass)}
            }
                
            response.check()
                
            ${insertIf(call.returnType.isMarkedNullable) {
                """
                if(response.status == HttpStatusCode.NoContent)
                    ~return@runCatching null!~
                """.trim()
                }}

            ${insertIf(call.returnType != resolver.builtIns.unitType) {
                val serializerText = call.returnType.getSerializerText()
                "decode(response.bodyAsText(), $serializerText, ${getCipherTextForReturn(ksclass)})"
            }}
            """

        is Call.Manual ->
            """
            val response = client.request("$fullPath") {
                ${getClientRequestPart(ksclass)}
            }

            response.check()

            ${if (call.tagType == resolver.builtIns.unitType)
                "response"
            else
                """
                ${getClientTagStatement(ksclass, call.tagType)}
                tag to response                        
                """.trim()
            }
            """

        is Call.PartialContent ->
            """
            val response = client.request("$fullPath") {
                ${getClientRequestPart(ksclass, "Head")}
            }
            
            response.check()
            
            ${if (call.tagType == resolver.builtIns.unitType)
                "val tag = Unit"
            else
                getClientTagStatement(ksclass, call.tagType)
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

        is Call.WebSocket ->{
            val sessionFunText =
                if (call.isRaw)
                    Decls().clientWebSocketRawSession
                else
                    Decls().clientWebSocketSession

            """
            client.$sessionFunText(path = "$pathWithoutBaseUrl"){
                enableWssIfNeeded(${call.isRaw})
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