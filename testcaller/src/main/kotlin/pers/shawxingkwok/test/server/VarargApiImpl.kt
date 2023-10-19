package pers.shawxingkwok.test.server

import io.ktor.server.application.*

class VarargApiImpl(override val call: ApplicationCall) : Phone.VarargApi {
    override suspend fun sumTime(vararg ints: Int): Int {
        return ints.sum()
    }
}