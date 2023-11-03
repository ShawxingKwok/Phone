package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

object SuperInterfaceApiImpl : Phone.SuperInterfaceApi {
    override suspend fun bar(): HttpResponser<Int> = {
        1
    }

    override suspend fun foo(): HttpResponser<Int> = {
        1
    }
}