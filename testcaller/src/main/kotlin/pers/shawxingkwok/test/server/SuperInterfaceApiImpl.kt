package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object SuperInterfaceApiImpl : Phone.SuperInterfaceApi {
    override suspend fun bar(): PipelineContextProvider<Int> = {
        1
    }

    override suspend fun foo(): PipelineContextProvider<Int> = {
        1
    }
}