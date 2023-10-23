@file:Suppress("PropertyName")

package pers.shawxingkwok.phone

import pers.shawxingkwok.ksputil.CodeFormatter

context (CodeFormatter)
class Types {
    val clientWebSocket get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.webSocket",
            innerName = null,
            isTopLevelAndExtensional = true
        )

    val clientWebSocketRaw get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.cio.webSocketRaw",
            innerName = null,
            isTopLevelAndExtensional = true
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

    val DefaultWebSocketClientSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.DefaultWebSocketClientSession",
            innerName = null,
            isTopLevelAndExtensional = false,
        )

    val WebSocketClientSession get() =
        getDeclText(
            outermostDeclPath = "io.ktor.client.plugins.websocket.WebSocketClientSession",
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
}