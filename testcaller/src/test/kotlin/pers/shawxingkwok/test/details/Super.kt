package pers.shawxingkwok.test.details

import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone

class Super {
    object Impl : Phone.SuperInterfaceApi {
        override suspend fun bar(): Callback<Int> = { 1 }

        override suspend fun foo(): Callback<Int> = { 1 }
    }

    @Test
    fun start() = testPhone(Impl){ phone ->
        assert(phone.SuperInterfaceApi().foo().getOrThrow() == 1)
        assert(phone.SuperInterfaceApi().bar().getOrThrow() == 1)
    }
}