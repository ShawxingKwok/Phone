package pers.shawxingkwok.test.server

import io.ktor.server.application.*

class PolymorphicApiImpl(override val call: ApplicationCall) :  Phone.PolymorphicApi{
    override suspend fun foo(): String {
        return "foo"
    }

    override suspend fun foo(i: Long): Long {
        return i
    }

    override suspend fun foo(j: Int): Int {
        return j
    }
}