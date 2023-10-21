package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

class PolymorphicApiImpl(override val context: PipelineContext<Unit, ApplicationCall>) :  Phone.PolymorphicApi{
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