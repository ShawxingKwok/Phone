package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object PolymorphicApiImpl :  Phone.PolymorphicApi{
    override suspend fun foo(): HttpResponser<String> = {
        "foo"
    }

    override suspend fun foo(i: Long): HttpResponser<Long> = {
        i
    }

    override suspend fun foo(j: Int): HttpResponser<Int> = {
        j
    }
}