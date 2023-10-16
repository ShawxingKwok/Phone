package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.WebSockets
@Phone.WebSockets.Protocol("a")
interface MyWebsocket {
    @Phone.WebSockets.Protocol("b")
    fun getChats()

    fun getContacts(id: Long)
}