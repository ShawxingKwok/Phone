package pers.shawxingkwok.test.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

interface WebSocketContract<T> {
    // other parameters could be put before t
    suspend fun get(t: T)
}

object WebSocketServerPhone{
    interface WebSocketApi : WebSocketContract<DefaultWebSocketServerSession>

    fun route(
        route: Route,
        getWebSocketApi: () -> WebSocketApi
    ){
        // may be raw
        route.webSocket("/ws"){
            getWebSocketApi().get(this)
        }
    }
}

class WebSocketApiImpl : WebSocketServerPhone.WebSocketApi {
    override suspend fun get(t: DefaultWebSocketServerSession) {
        val text = t.incoming.receive() as Frame.Text
        t.send(text)
    }
}

class WebSocketClientPhone(val client: HttpClient) {
    inner class WebSocketServiceImpl(
        private val extendRequest: (HttpRequestBuilder.() -> Unit)? = null,
    )
        : WebSocketContract<suspend DefaultClientWebSocketSession.() -> Unit>
    {
        override suspend fun get(t: suspend DefaultClientWebSocketSession.() -> Unit) {
            client.webSocket("ws", block = t)
        }
    }
}