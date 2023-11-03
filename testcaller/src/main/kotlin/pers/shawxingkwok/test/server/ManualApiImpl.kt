package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*


object ManualApiImpl :  Phone.ManualApi {
    override suspend fun directGet(): Callback<Long> = {
        1
    }

    override suspend fun exchange(id: String): Callback<List<String>> = {
        val channel = call.receiveChannel()
        call.respond(channel)
        listOf(id)
    }

    override suspend fun getIdLength(id: String?): Callback<Int?> = {
        id?.length
    }

    override suspend fun getUnit(id: String): Callback<Unit> = {

    }
}