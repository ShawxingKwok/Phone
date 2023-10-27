package expect

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import pers.shawxingkwok.expect.server.*
import java.io.File

object TestApiImpl : Phone.TestApi {
    override fun Route.doOtherTasks() {}

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

    override suspend fun getFile(id: String): ManualConnector<Int?> {
        return id.length to {
            val bytes = call.receiveChannel().toByteArray()
            call.respondBytes(bytes)
        }
    }

    override suspend fun getPartialFile(id: String): CommonConnector<Pair<Int?, File>> =
    {
        val file = File(id)
        file.readBytes().size to file
    }
}