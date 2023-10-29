package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    @Phone.Auth
    interface Partial{
        @Phone.Call.Common<Unit>
        suspend fun delete(id: Long): Any?

        @Phone.Call.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    interface Whole{
        @Phone.Call.Common<Unit>
        @Phone.Auth(["auth-basic"])
        suspend fun delete(id: Long): Any?

        @Phone.Call.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    interface Multi{
        // TODO(is this allowed?)
        @Phone.Call.Common<Int>
        @Phone.Auth(["auth-basic"], Phone.Auth.Strategy.Required)
        suspend fun get(): Any?

        @Phone.Call.Common<Unit>
        suspend fun delete(id: Long): Any?

        @Phone.Call.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    @Phone.Auth(["auth-basic"])
    interface Jwt{
        @Phone.Auth(["auth-jwt"], withToken = true)
        @Phone.Call.Common<Boolean>
        suspend fun delete(id: String): Any?
    }
}