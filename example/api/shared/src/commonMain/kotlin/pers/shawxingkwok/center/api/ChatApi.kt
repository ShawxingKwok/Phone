package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface ChatApi {
    @Phone.WebSocket
    suspend fun getChats(): Any?
}