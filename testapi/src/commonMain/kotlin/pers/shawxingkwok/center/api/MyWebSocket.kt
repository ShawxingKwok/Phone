package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.WebSocket("A")
interface MyWebSocket {
    suspend fun getChats()

    suspend fun getContacts(id: Long)
}