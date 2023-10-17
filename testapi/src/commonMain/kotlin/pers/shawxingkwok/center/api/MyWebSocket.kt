package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.WebSocket
interface MyWebSocket {
    suspend fun getChats()
}

@Phone.WebSocket(isRaw = true)
interface MyRawWebSocket {
    suspend fun getChats()
}

@Phone.WebSocket("A")
interface MySubProtocolWebSocket {
    suspend fun getChats()
}

@Phone.WebSocket
interface MyWebSocketWithArgs {
    suspend fun getChats(id: Long, name: String)
}