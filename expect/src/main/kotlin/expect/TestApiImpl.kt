package expect

import io.ktor.client.plugins.websocket.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import pers.shawxingkwok.expect.server.*

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

    override suspend fun getFile(id: String): FileConnector<Int?> {
        return id.length to {
            val bytes = call.receiveChannel().toByteArray()
            call.respondBytes(bytes)
        }
    }
}