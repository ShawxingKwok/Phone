@file:Suppress("PropertyName")

package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.CodeFormatter

context (CodeFormatter)
class Decls {
    val EmptyContent get() = getDeclText(outermostDeclPath = "io.ktor.client.utils.EmptyContent")

    val serverWebSocket get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.websocket.webSocket",
            isTopLevelAndExtensional = true
        )

    val serverWebSocketRaw get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.websocket.webSocketRaw",
            isTopLevelAndExtensional = true
        )

    val ApplicationCall get() = getDeclText(outermostDeclPath = "io.ktor.server.application.ApplicationCall")

    val PipelineContextUnitCall get() = getDeclText(outermostDeclPath = "io.ktor.util.pipeline.PipelineContext")
            .plus("<Unit, $ApplicationCall>")

    val DefaultWebSocketServerSession get() = getDeclText(outermostDeclPath = "io.ktor.server.websocket.DefaultWebSocketServerSession")

    val WebSocketServerSession get() = getDeclText(outermostDeclPath = "io.ktor.server.websocket.WebSocketServerSession")

    val DefaultClientWebSocketSession get() = getDeclText(outermostDeclPath = "io.ktor.client.plugins.websocket.DefaultClientWebSocketSession")

    val ClientWebSocketSession get() = getDeclText(outermostDeclPath = "io.ktor.client.plugins.websocket.ClientWebSocketSession")

    val authenticate get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.auth.authenticate",
            isTopLevelAndExtensional = true
        )

    val AuthenticationStrategy get() = getDeclText(outermostDeclPath = "io.ktor.server.auth.AuthenticationStrategy")

    @get:JvmName("fun clientWebSocketSession")
    val clientWebSocketSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.webSocketSession",
            isTopLevelAndExtensional = true,
        )

    @get:JvmName("fun clientWebSocketRawSession")
    val clientWebSocketRawSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.cio.webSocketRawSession",
            isTopLevelAndExtensional = true,
        )

    val receiveParameters get() =
        getDeclText(
            outermostDeclPath = "io.ktor.server.request.receiveParameters",
            isTopLevelAndExtensional = true
        )

    val CloseReason get() = getDeclText(outermostDeclPath = "io.ktor.websocket.CloseReason")

    val close get() =
        getDeclText(
            outermostDeclPath = "io.ktor.websocket.close",
            isTopLevelAndExtensional = true
        )
}