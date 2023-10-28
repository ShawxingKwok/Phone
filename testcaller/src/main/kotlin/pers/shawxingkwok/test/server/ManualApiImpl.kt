package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*


object ManualApiImpl :  Phone.ManualApi {
    override suspend fun directGet(): CommonConnector<Long> = {
        1
    }

    override suspend fun exchange(id: String): CommonConnector<List<String>> = {
        val receivedBytes = call.receive<ByteArray>()
        call.respond(receivedBytes)
        listOf(id)
    }

    override suspend fun getIdLength(id: String?): CommonConnector<Int?> = {
        id?.length
    }

    override suspend fun getUnit(id: String): CommonConnector<Unit> = {

    }
}