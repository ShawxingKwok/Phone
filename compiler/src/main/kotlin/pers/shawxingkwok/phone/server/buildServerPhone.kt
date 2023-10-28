package pers.shawxingkwok.phone.server

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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
        typealias CommonConnector<T> = suspend ${Decls().PipelineContextUnitCall}.() -> T
        typealias ManualConnector<T> = Pair<T, CommonConnector<Unit>>
        ${insertIf(MyProcessor.hasWebSocket){
            """
            typealias WebSocketConnector = suspend ${Decls().DefaultWebSocketServerSession}.() -> Unit
            typealias WebSocketRawConnector = suspend ${Decls().WebSocketServerSession}.() -> Unit
            """
        }}
        ${insertIf(MyProcessor.hasPartialContent){
            "typealias PartialContentConnector<T> = CommonConnector<Pair<T, ${Decls().File}>>"
        }}
        
        object Phone{
            ${MyProcessor.phones.joinToString(""){ ksclass ->
                """
                interface ${ksclass.apiNameInPhone} : ${ksclass.qualifiedName()}{
                    fun Route.doOtherTasks(){}
                    
                    ${ksclass.getNeededFunctions().joinToString("\n\n"){ ksfun ->
                        val returnedText = when(val kind = ksfun.kind) {
                            is Kind.Common -> "CommonConnector<${kind.returnType.text}>"
                            is Kind.Manual -> "ManualConnector<${kind.tagType}>"
                            is Kind.PartialContent -> "PartialContentConnector<${kind.tagType}>"
                            is Kind.WebSocket -> 
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
private fun KSFunctionDeclaration.getBody(ksclass: KSClassDeclaration) =
    mayEmbraceWithAuth(this) {
        when(val kind = kind){
            is Kind.Common -> getServerCommonContent(ksclass, kind)
            is Kind.WebSocket -> getServerWebSocketContent(ksclass, kind)
            is Kind.Manual -> ""
            is Kind.PartialContent -> getServerPartialContent(ksclass, kind)
        }
    }