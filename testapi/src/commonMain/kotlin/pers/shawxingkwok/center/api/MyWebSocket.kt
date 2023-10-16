package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.WebSockets("A")
interface MyWebSocket {
    suspend fun getChats()

    suspend fun getContacts(id: Long)
}