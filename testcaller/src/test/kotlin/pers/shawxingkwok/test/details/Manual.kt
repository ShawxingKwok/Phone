package pers.shawxingkwok.test.details

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import org.junit.Assert.assertNull
import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone
import java.io.File

class Manual {
    object Impl : Phone.ManualApi {
        override suspend fun directGet(): Callback<Long> = { 1 }

        override suspend fun getIdLength(id: String?): Callback<Int?> = { id?.length }

        override suspend fun getUnit(id: String): Callback<Unit> = { }

        override suspend fun exchange(id: String): Callback<List<String>> = {
            val channel = call.receiveChannel()
            call.respond(channel)
            listOf(id)
        }
    }

    @Test
    fun start() = testPhone(Impl) { phone ->
        phone.ManualApi()
            .directGet()
            .getOrThrow()
            .let {
                assert(it.first == 1L)
            }

        phone.ManualApi()
            .getIdLength("2")
            .getOrThrow()
            .let { (size, _) ->
                assert(size == 1)
            }

        phone.ManualApi()
            .getIdLength(null)
            .getOrThrow()
            .let { (size, _) ->
                assertNull(size)
            }

        phone.ManualApi().getUnit("S").getOrThrow()

        val file = File(".gitignore")
        phone.ManualApi {
                val channel = file.readChannel()
                setBody(channel)
            }
            .exchange("122")
            .getOrThrow()
            .let { (headInfo, response) ->
                assert(headInfo == listOf("122")) {
                    headInfo
                }
                val bytes = response.readBytes()
                assert(bytes.contentEquals(file.readBytes())) {
                    bytes.joinToString()
                }
            }
    }
}