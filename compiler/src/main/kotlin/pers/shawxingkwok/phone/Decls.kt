@file:Suppress("PropertyName")

package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.CodeFormatter

context (CodeFormatter)
class Decls {
    val File = getDeclText(
        outermostDeclPath = "java.io.File",
        innerName = null,
        isTopLevelAndExtensional = false
    )
    val serverWebSocket get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.websocket.webSocket",
            innerName = null,
            isTopLevelAndExtensional = true
        )

    val serverWebSocketRaw get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.websocket.webSocketRaw",
            innerName = null,
            isTopLevelAndExtensional = true
        )

    val ApplicationCall get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.application.ApplicationCall",
            innerName = null,
            isTopLevelAndExtensional = false,
        )

    val PipelineContextUnitCall get() =
        getDeclText(
            outermostDeclPath = "io.ktor.util.pipeline.PipelineContext",
            innerName = null,
            isTopLevelAndExtensional = false,
        )
        .plus("<Unit, $ApplicationCall>")

    val DefaultWebSocketServerSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.websocket.DefaultWebSocketServerSession",
            innerName = null,
            isTopLevelAndExtensional = false,
        )

    val WebSocketServerSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.websocket.WebSocketServerSession",
            innerName = null,
            isTopLevelAndExtensional = false,
        )

    val DefaultClientWebSocketSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.DefaultClientWebSocketSession",
            innerName = null,
            isTopLevelAndExtensional = false,
        )

    val ClientWebSocketSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.ClientWebSocketSession",
            innerName = null,
            isTopLevelAndExtensional = false,
        )

    val authenticate get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.auth.authenticate",
            innerName = null,
            isTopLevelAndExtensional = true
        )

    val AuthenticationStrategy get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.auth.AuthenticationStrategy",
            innerName = null,
            isTopLevelAndExtensional = false
        )

    @get:JvmName("fun clientWebSocketSession")
    val clientWebSocketSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.webSocketSession",
            innerName = null,
            isTopLevelAndExtensional = true,
        )

    @get:JvmName("fun clientWebSocketRawSession")
    val clientWebSocketRawSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.cio.webSocketRawSession",
            innerName = null,
            isTopLevelAndExtensional = true,
        )

    val receiveParameters get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.request.receiveParameters",
            innerName = null,
            isTopLevelAndExtensional = true
        )

    val CloseReason get() =
        getDeclText(
            outermostDeclPath = "io.ktor.websocket.CloseReason",
            innerName = null,
            isTopLevelAndExtensional = false
        )

    val close get() =
        getDeclText(
            outermostDeclPath = "io.ktor.websocket.close",
            innerName = null,
            isTopLevelAndExtensional = true
        )
}