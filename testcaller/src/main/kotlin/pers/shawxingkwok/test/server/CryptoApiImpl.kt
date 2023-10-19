package pers.shawxingkwok.test.server

import io.ktor.server.application.*

object CryptoApiImpl {
    class Partial(override val call: ApplicationCall) : Phone.CryptoApi_Partial {
        override suspend fun getChats(id: Long, name: String, password: String): List<String> {
            return listOf(id.toString(), name, password)
        }
    }

    class Whole(override val call: ApplicationCall) : Phone.CryptoApi_Whole {
        override suspend fun getChats(id: Long, name: String): List<String> {
            return listOf(id.toString(), name)
        }
    }
}