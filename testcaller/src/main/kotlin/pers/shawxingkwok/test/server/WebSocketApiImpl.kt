package pers.shawxingkwok.test.server

import io.ktor.server.websocket.*

class WebSocketApiImpl : Phone.WebSocketApi {
    override suspend fun get(i: Int, t: DefaultWebSocketServerSession) {
        TODO("Not yet implemented")
    }
}