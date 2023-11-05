package pers.shawxingkwok.test.details

import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.response.*
import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone
import java.io.File

private val file = File(".gitignore")

class PartialContent {
    object Impl : Phone.PartialContentApi {
        override suspend fun partialGet(id: String): Callback<Pair<String, Long>> = {
            // checkRequest(id.none()){}
            val length = file.length()
            call.respondFile(file)
            id to length
        }

        override suspend fun partialGetUnit(id: String): Callback<Unit> = {
            call.respondFile(File(".gitignore"))
        }
    }

    @Test
    fun start() = testPhone(
        api = Impl,
        configureServer = {
            install(PartialContent) {
                maxRangeCount = 10
            }
            install(AutoHeadResponse)
        }
    ){phone ->
        val path = file.path
        val expectedBytes = file.readBytes()

        phone.PartialContentApi()
            .partialGet(path)
            .getOrThrow()
            .let {
                assert(it.tag.first == path)
                assert(it.tag.second == expectedBytes.size.toLong())
                assert(it.get().readBytes().contentEquals(expectedBytes))
                val partialBytes = it.get(0L..<2L, 2L..<3L).readBytes()

                assert(partialBytes.contentEquals(expectedBytes.take(3).toByteArray())) {
                    partialBytes.toList()
                }
            }

        phone.PartialContentApi()
            .partialGetUnit("@")
            .getOrThrow()
            .let {
                assert(it.get().readBytes().contentEquals(expectedBytes))
            }
    }
}