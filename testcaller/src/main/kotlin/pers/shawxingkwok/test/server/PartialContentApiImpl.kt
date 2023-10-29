package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.response.*
import java.io.File

object PartialContentApiImpl : Phone.PartialContentApi {
    override suspend fun partialGet(id: String): PipelineContextProvider<Pair<String, Long>> = {
        val file = File(".gitignore")
        val length = file.length()
        call.respondFile(file)
        id to length
    }

    override suspend fun partialGetUnit(id: String): PipelineContextProvider<Unit> = {
        call.respondFile(File(".gitignore"))
    }
}