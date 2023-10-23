package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.WebSocket
interface WebSocketApi<T> {
    suspend fun get(i: Int, t: T)
}