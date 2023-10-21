package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.CodeFormatter
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ktutil.updateIf

context (CodeFormatter)
internal fun KSClassDeclaration.getInterfacePropStatement(): String =
    when(getAnnotationByType(Phone.WebSocket::class)) {
        null -> """
            val context: ${getInterfacePropTypeText()} 
            suspend fun handle(){}
            """.trim()

        else -> "val session: ${getInterfacePropTypeText()}"
    }

context (CodeFormatter)
internal fun KSClassDeclaration.getInterfacePropTypeText(): String =
    when(val websocketsAnnot = getAnnotationByType(Phone.WebSocket::class)) {
        null -> "PipelineContext<Unit, ApplicationCall>"
        else -> getDeclText(
            import = "io.ktor.server.websocket.${insertIf(!websocketsAnnot.isRaw) { "Default" }}WebSocketServerSession",
            innerName = null,
            isTopLevelAndExtensional = false
        )
    }