package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface WebSocketApi {
    @Phone.Kind.WebSocket
    suspend fun getSignals(i: Int): Any?

    @Phone.Method.Post
    @Phone.Kind.WebSocket(true)
    suspend fun getChats(id: String?): Any?
}