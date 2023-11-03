package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import pers.shawxingkwok.center.api.AccountApi
import pers.shawxingkwok.center.api.AuthApi
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

object AuthApiImpl{
    object Partial : Phone.AuthApi_Partial{
        override suspend fun delete(id: Int): Callback<Int> =
        {
            id
        }
    }

    object Whole : Phone.AuthApi_Whole{
        override suspend fun delete(id: Int): Callback<Int> =
        {
            id
        }
    }

    object Multi : Phone.AuthApi_Multi{
        override suspend fun get(id: Int): Callback<Int> =
        {
            id
        }
    }

    object Jwt : Phone.AuthApi_Jwt{
        override suspend fun delete(id: Int): Callback<Int> =
        {
            id
        }
    }
}