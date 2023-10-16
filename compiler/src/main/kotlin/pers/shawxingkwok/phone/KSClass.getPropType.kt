package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.KtGen
import pers.shawxingkwok.ksputil.getAnnotationByType

context (KtGen)
internal fun KSClassDeclaration.getPropStatement(isServerSide: Boolean): String =
    if (isServerSide)
        when(val websocketsAnnot = getAnnotationByType(Phone.WebSocket::class)) {
            null -> "val call: ApplicationCall"
            else -> "val session: ${getPropTypeText(true)}"
        }
    else TODO()

context (KtGen)
internal fun KSClassDeclaration.getPropTypeText(isServerSide: Boolean): String =
    if (isServerSide)
        when(val websocketsAnnot = getAnnotationByType(Phone.WebSocket::class)) {
            null -> "ApplicationCall"
            else -> getDeclText(
                import = "io.ktor.server.websocket.${insertIf(!websocketsAnnot.isRaw) { "Default" }}WebSocketServerSession",
                innerName = null,
                isTopLevelAndExtensional = false
            )
        }
    else TODO()