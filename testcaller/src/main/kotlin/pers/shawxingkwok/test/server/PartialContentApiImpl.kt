package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.response.*
import java.io.File

object PartialContentApiImpl : Phone.PartialContentApi {
    override suspend fun partialGet(id: String): PartialContentConnector<Pair<String, Long>> = {
        val file = File(".gitignore")
        val length = file.length()

        (id to length) to file
    }

    override suspend fun partialGetUnit(id: String): PartialContentConnector<Unit> = {
        Unit to File(".gitignore")
    }
}