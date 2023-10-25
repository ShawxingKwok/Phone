package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import pers.shawxingkwok.center.api.AccountApi
import pers.shawxingkwok.center.api.AuthApi
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

object AuthApiImpl{
    object Partial : Phone.AuthApi_Partial{
        override suspend fun delete(id: Long): CommonConnector<Unit> =
        {

        }

        override suspend fun search(id: Long): CommonConnector<User?> =
        {
            User(id, "Shawxing", 25)
        }
    }

    object Whole : Phone.AuthApi_Whole{
        override suspend fun delete(id: Long): CommonConnector<Unit> =
        {

        }

        override suspend fun search(id: Long): CommonConnector<User?> =
        {
            User(id, "Shawxing", 25)
        }
    }

    object Multi : Phone.AuthApi_Multi{
        override suspend fun get(): CommonConnector<Int> =
        {
            1
        }

        override suspend fun delete(id: Long): CommonConnector<Unit> =
        {

        }

        override suspend fun search(id: Long): CommonConnector<User?> =
        {
            User(id, "Shawxing", 25)
        }
    }

    object Jwt : Phone.AuthApi_Jwt{
        override suspend fun delete(id: String): CommonConnector<Boolean> =
        {
            true
        }
    }
}