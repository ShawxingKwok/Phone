package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import pers.shawxingkwok.center.model.User

object AuthApiImpl{
    class Partial(override val call: ApplicationCall) : Phone.AuthApi_Partial{
        override suspend fun delete(id: Long) {
        }

        override suspend fun search(id: Long): User? {
            return User(id, "Shawxing", 25)
        }
    }

    class Whole(override val call: ApplicationCall) : Phone.AuthApi_Whole{
        override suspend fun delete(id: Long) {
        }

        override suspend fun search(id: Long): User? {
            return User(id, "Shawxing", 25)
        }
    }
}