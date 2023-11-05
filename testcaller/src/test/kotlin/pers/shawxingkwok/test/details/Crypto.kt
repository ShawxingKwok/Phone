package pers.shawxingkwok.test.details

import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone

class Crypto {
    @Test
    fun partial() = testPhone(
        object : Phone.CryptoApi_Partial {
            override suspend fun getChats(id: Long, name: String, password: String): Callback<List<String>> =
                {
                    listOf("$id", name, password)
                }

            override suspend fun _getChats(id: Long, name: String): Callback<List<String>> =
                {
                    listOf("$id", name)
                }
        }
    ) { phone ->
        assert(phone.CryptoApi_Partial().getChats(1, "a", "b").getOrThrow() == listOf("1", "a", "b"))
        assert(phone.CryptoApi_Partial()._getChats(1, "a").getOrThrow() == listOf("1", "a"))
    }

    @Test
    fun whole() = testPhone(
        object : Phone.CryptoApi_Whole {
            override suspend fun getChats(id: Long, name: String): Callback<List<String>> =
                {
                    listOf("$id", name)
                }
        }
    ) { phone ->
        phone.CryptoApi_Whole()
            .getChats(1, "a")
            .getOrThrow()
            .let {
                assert(it == listOf("1", "a"))
            }
    }
}