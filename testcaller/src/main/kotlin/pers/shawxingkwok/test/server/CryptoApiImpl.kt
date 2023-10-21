package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object CryptoApiImpl {
    class Partial(override val context: PipelineContext<Unit, ApplicationCall>) : Phone.CryptoApi_Partial {
        override suspend fun getChats(id: Long, name: String, password: String): List<String> {
            return listOf(id.toString(), name, password)
        }
    }

    class Whole(override val context: PipelineContext<Unit, ApplicationCall>) : Phone.CryptoApi_Whole {
        override suspend fun getChats(id: Long, name: String): List<String> {
            return listOf(id.toString(), name)
        }
    }
}