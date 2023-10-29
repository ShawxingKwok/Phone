package server

import pers.shawxingkwok.server.phone.Phone
import pers.shawxingkwok.server.phone.WebSocketConnector

object ChatApiImpl : Phone.ChatApi {
    override suspend fun getChats(): WebSocketConnector = {
        listOf("Hello, world!")
    }
}