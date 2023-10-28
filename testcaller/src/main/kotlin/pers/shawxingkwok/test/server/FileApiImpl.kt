package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.response.*
import java.io.File

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

    override suspend fun partialGet(id: String): PartialContentConnector<Pair<String, Long>> = {
        val file = File(".gitignore")
        val length = file.length()

        (id to length) to file
    }
}