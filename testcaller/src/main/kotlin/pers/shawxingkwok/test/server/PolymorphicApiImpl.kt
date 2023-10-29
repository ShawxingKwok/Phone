package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object PolymorphicApiImpl :  Phone.PolymorphicApi{
    override suspend fun foo(): PipelineContextProvider<String> = {
        "foo"
    }

    override suspend fun foo(i: Long): PipelineContextProvider<Long> = {
        i
    }

    override suspend fun foo(j: Int): PipelineContextProvider<Int> = {
        j
    }
}