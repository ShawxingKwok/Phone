package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*


object ManualApiImpl :  Phone.ManualApi {
    override suspend fun directGet(): HttpResponser<Long> = {
        1
    }

    override suspend fun exchange(id: String): HttpResponser<List<String>> = {
        val channel = call.receiveChannel()
        call.respond(channel)
        listOf(id)
    }

    override suspend fun getIdLength(id: String?): HttpResponser<Int?> = {
        id?.length
    }

    override suspend fun getUnit(id: String): HttpResponser<Unit> = {

    }
}