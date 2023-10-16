package pers.shawxingkwok.test.server

import io.ktor.server.websocket.*

class MyWebSocketImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocket {
    override suspend fun getChats() {
        TODO("Not yet implemented")
    }

    override suspend fun getContacts(id: Long) {
        TODO("Not yet implemented")
    }
}