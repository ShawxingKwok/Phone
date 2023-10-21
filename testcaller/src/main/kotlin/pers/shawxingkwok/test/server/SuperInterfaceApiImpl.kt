package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

class SuperInterfaceApiImpl(override val context: PipelineContext<Unit, ApplicationCall>) : Phone.SuperInterfaceApi {
    override suspend fun bar(): Int {
        return 1
    }

    override suspend fun foo(): Int {
        return 1
    }
}