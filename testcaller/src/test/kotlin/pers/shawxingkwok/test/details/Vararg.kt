package pers.shawxingkwok.test.details

import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone

class Vararg {
    object Impl : Phone.VarargApi{
        override suspend fun sum(vararg ints: Int): Callback<Int> =
        {
            ints.sum()
        }
    }
    @Test
    fun start() = testPhone(Impl) {
        assert(it.VarargApi().sum(1, 2, 3).getOrThrow() == 6)
    }
}