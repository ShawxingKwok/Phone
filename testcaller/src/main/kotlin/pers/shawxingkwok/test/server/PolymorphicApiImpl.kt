package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object PolymorphicApiImpl :  Phone.PolymorphicApi{
    override suspend fun foo(): CommonConnector<String> = {
        "foo"
    }

    override suspend fun foo(i: Long): CommonConnector<Long> = {
        i
    }

    override suspend fun foo(j: Int): CommonConnector<Int> = {
        j
    }
}