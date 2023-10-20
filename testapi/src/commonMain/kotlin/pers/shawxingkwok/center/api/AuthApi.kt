package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    @Phone.Auth
    interface Partial{
        suspend fun delete(id: Long)
        suspend fun search(id: Long): User?
    }

    @Phone.Api
    interface Whole{
        @Phone.Auth(["auth-basic"])
        suspend fun delete(id: Long)
        suspend fun search(id: Long): User?
    }

    @Phone.Api
    interface Multi{
        // is this allowed?
        @Phone.Auth(["auth-basic"], Phone.Auth.Strategy.Required)
        suspend fun get(): Int

        suspend fun delete(id: Long)
        suspend fun search(id: Long): User?
    }

    @Phone.Api
    @Phone.Auth(["auth-basic"])
    interface Jwt{
        @Phone.Auth(["auth-jwt"], withToken = true)
        suspend fun delete(id: String): Boolean
    }
}