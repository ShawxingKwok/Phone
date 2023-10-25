package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object VarargApiImpl : Phone.VarargApi {
    override suspend fun sum(vararg ints: Int): CommonConnector<Int> =
    {
        ints.sum()
    }
}