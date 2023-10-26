package pers.shawxingkwok.test.server

import io.ktor.server.websocket.*
import io.ktor.websocket.*

object WebSocketApiImpl : Phone.WebSocketApi{
    override suspend fun getSignals(i: Int): WebSocketConnector = {
        send("$i")
    }

    override suspend fun getChats(id: String?): WebSocketRawConnector = {
        send("$id")
    }
}