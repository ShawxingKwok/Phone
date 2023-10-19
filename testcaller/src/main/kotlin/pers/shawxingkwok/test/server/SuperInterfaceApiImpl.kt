package pers.shawxingkwok.test.server

import io.ktor.server.application.*

class SuperInterfaceApiImpl(override val call: ApplicationCall) : Phone.SuperInterfaceApi {
    override suspend fun bar(): Int {
        return 1
    }

    override suspend fun foo(): Int {
        return 1
    }
}