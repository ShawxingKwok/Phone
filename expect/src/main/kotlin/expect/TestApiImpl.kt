package expect

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay

object TestApiImpl : WebSocketServerPhone.TestApi{
    override suspend fun get(i: Int): WebSocketConnector = {
        send("$i")
        while (true) delay(100)
    }

    override suspend fun obtain(i: Int): WebSocketRawConnector = {
        send("$i")
    }

    override suspend fun search(id: String): CommonConnector<User?> = {
        User(id)
    }

    override suspend fun delete(id: String): Any? {
        TODO("Not yet implemented")
    }
}