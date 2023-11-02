package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface WebSocketApi {
    @Phone.Call.WebSocket
    suspend fun getSignals(i: Int): Any

    @Phone.Call.WebSocket(Phone.Method.Post, isRaw = true)
    suspend fun getChats(id: String?): Any
}