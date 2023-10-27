package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.response.*

object FileApiImpl : Phone.FileApi {
    override suspend fun exchange(id: String): ManualConnector<List<String>> {
        return listOf(id) to {
            call.respondBytes(byteArrayOf(1))
        }
    }

    override suspend fun get(id: String): ManualConnector<Int?> {
        return null to {
            call.respondBytes(byteArrayOf(1))
        }
    }
}