package expect

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import pers.shawxingkwok.expect.server.CommonConnector
import pers.shawxingkwok.expect.server.Phone
import pers.shawxingkwok.expect.server.WebSocketConnector
import pers.shawxingkwok.expect.server.WebSocketRawConnector

object TestApiImpl : Phone.TestApi{
    override suspend fun get(i: Int): WebSocketConnector = {
        send("$i")
    }

    override suspend fun obtain(i: Int): WebSocketRawConnector = {
        send("$i")
    }

    override suspend fun search(id: String): CommonConnector<User?> = {
        User(id)
    }

    override suspend fun delete(id: String): CommonConnector<Unit> = {

    }
}