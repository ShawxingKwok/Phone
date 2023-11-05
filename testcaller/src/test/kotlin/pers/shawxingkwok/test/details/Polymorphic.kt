package pers.shawxingkwok.test.details

import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone

class Polymorphic {
    object Impl :  Phone.PolymorphicApi{
        override suspend fun foo(): Callback<String> = { "foo" }

        override suspend fun foo(i: Long): Callback<Long> = { i }

        override suspend fun foo(j: Int): Callback<Int> = { j }
    }

    @Test
    fun start() = testPhone(Impl) { phone ->
        assert(phone.PolymorphicApi().foo().getOrThrow() == "foo")
        assert(phone.PolymorphicApi().foo(1L).getOrThrow() == 1L)
        assert(phone.PolymorphicApi().foo(1).getOrThrow() == 1)
    }
}