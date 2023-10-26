package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    @Phone.Auth
    interface Partial{
        @Phone.Common<Unit>
        suspend fun delete(id: Long): Any?

        @Phone.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    interface Whole{
        @Phone.Common<Unit>
        @Phone.Auth(["auth-basic"])
        suspend fun delete(id: Long): Any?

        @Phone.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    interface Multi{
        // TODO(is this allowed?)
        @Phone.Common<Int>
        @Phone.Auth(["auth-basic"], Phone.Auth.Strategy.Required)
        suspend fun get(): Any?

        @Phone.Common<Unit>
        suspend fun delete(id: Long): Any?

        @Phone.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    @Phone.Auth(["auth-basic"])
    interface Jwt{
        @Phone.Common<Boolean>
        @Phone.Auth(["auth-jwt"], withToken = true)
        suspend fun delete(id: String): Any?
    }
}