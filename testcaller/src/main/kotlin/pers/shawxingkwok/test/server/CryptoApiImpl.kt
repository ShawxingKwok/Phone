package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object CryptoApiImpl {
    object Partial : Phone.CryptoApi_Partial {
        override suspend fun getChats(id: Long, name: String, password: String): CommonConnector<List<String>> =
        {
            listOf(id.toString(), name, password)
        }
    }

    object Whole : Phone.CryptoApi_Whole {
        override suspend fun getChats(id: Long, name: String): CommonConnector<List<String>> =
        {
            listOf(id.toString(), name)
        }
    }
}