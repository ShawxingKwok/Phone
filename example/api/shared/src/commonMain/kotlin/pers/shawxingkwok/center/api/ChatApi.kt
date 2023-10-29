package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface ChatApi {
    @Phone.Call.WebSocket
    suspend fun getChats(): Any
}