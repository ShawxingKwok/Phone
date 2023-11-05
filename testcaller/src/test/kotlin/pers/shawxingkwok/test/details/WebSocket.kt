package pers.shawxingkwok.test.details

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.junit.Test
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.server.WebSocketConnector
import pers.shawxingkwok.test.server.WebSocketRawConnector
import pers.shawxingkwok.test.util.assertAll
import pers.shawxingkwok.test.util.testPhone

class WebSocket {
    object Impl : Phone.WebSocketApi {
        override suspend fun getSignals(i: Int): WebSocketConnector = { send("$i") }

        override suspend fun getChats(id: String?): WebSocketRawConnector = { send("$id") }
    }

    fun websocket(enablesWss: Boolean) = testPhone(
        api = Impl,
        configureServer = {
            install(WebSockets)
        },
        configureClient = {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }
    ) { phone ->
        phone.WebSocketApi()
            .getSignals(1)
            .getOrThrow()
            .run {
                val text = (incoming.receive() as Frame.Text).readText()
                assert(text == "1"){ text }
            }

        phone.WebSocketApi()
            .getChats("1")
            .getOrThrow()
            .run {
                val text = (incoming.receive() as Frame.Text).readText()
                assert(text == "1"){ text }
            }
    }

    @Test
    fun start(){
        assertAll(::websocket, listOf(false), listOf(true))
    }
}