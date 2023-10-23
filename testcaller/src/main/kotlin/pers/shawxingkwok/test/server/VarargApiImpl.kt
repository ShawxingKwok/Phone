package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

class VarargApiImpl(override val context: PipelineContext<Unit, ApplicationCall>) : Phone.VarargApi {
    override suspend fun sum(vararg ints: Int): Int {
        return ints.sum()
    }
}