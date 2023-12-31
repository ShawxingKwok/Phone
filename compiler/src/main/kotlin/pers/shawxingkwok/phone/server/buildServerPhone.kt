package pers.shawxingkwok.phone.server

import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.phone.*
import pers.shawxingkwok.phone.Args
import pers.shawxingkwok.phone.MyProcessor
import pers.shawxingkwok.phone.createFile
import pers.shawxingkwok.phone.getCoderFunctions
import pers.shawxingkwok.phone.insertIf
import pers.shawxingkwok.phone.server.parts.mayEmbraceWithAuth

internal fun buildServerPhone() {
    createFile(
        phones = MyProcessor.phones,
        packageName = Args.ServerPackageName,
        initialImports = setOf(
            "pers.shawxingkwok.phone.Phone",
            "io.ktor.http.*",
            "io.ktor.server.application.*",
            "io.ktor.server.response.*",
            "io.ktor.server.routing.*",
            "io.ktor.util.pipeline.PipelineContext",
            "kotlinx.serialization.json.Json",
            "kotlinx.serialization.encodeToString",
            "kotlinx.serialization.KSerializer",
            "kotlinx.serialization.builtins.ByteArraySerializer",
        ),
    ){
        """
        typealias Callback<T> = suspend PipelineContext<Unit, ApplicationCall>.() -> T
        ${insertIf(MyProcessor.hasWebSocket){
            """
            typealias WebSocketConnector = suspend ${Decls().DefaultWebSocketServerSession}.() -> Unit
            typealias WebSocketRawConnector = suspend ${Decls().WebSocketServerSession}.() -> Unit
            """
        }}
        
        object Phone{
            ${MyProcessor.phones.joinToString(""){ ksclass ->
                """
                interface ${ksclass.apiNameInPhone} : ${ksclass.qualifiedName()}{
                    fun Route.onStart(){}
                    
                    ${ksclass.getNeededFunctions().joinToString("\n\n"){ ksfun ->
                        val returnedText = when(val kind = ksfun.getCall(ksclass)) {
                            is Call.Common -> "Callback<${kind.returnType.text}>"
                            is Call.Manual -> "Callback<${kind.tagType}>"
                            is Call.PartialContent -> "Callback<${kind.tagType}>"
                            is Call.WebSocket -> 
                                if (kind.isRaw)
                                    "WebSocketRawConnector"
                                else
                                    "WebSocketConnector"
                        }
                        ksfun.getHeader(returnedText)  
                    }}
                }
                """
            }}
            
            ${getCoderFunctions()}
    
            private fun PipelineContext<Unit, ApplicationCall>.respondBadRequest(text: String){
                val code = HttpStatusCode(400, text)
                call.response.status(code)
            }
            
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
                        ${ksclass.apiPropNameInPhone}.run { onStart() }
                        
                        ${ksclass.mayEmbraceWithAuth {
                            ksclass.getNeededFunctions().joinToString("\n\n") { ksfun ->
                                ksfun.mayEmbraceWithAuth {
                                    ksfun.getServerRouteContent(ksclass)                                    
                                }
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