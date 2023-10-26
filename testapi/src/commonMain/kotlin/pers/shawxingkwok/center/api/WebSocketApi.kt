package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface WebSocketApi {
    @Phone.WebSocket
    suspend fun getSignals(i: Int): Any?

    @Phone.WebSocket(true)
    suspend fun getChats(id: String?): Any?
}