package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    @Phone.Feature.Auth
    interface Partial{
        @Phone.Kind.Common<Unit>
        suspend fun delete(id: Long): Any?

        @Phone.Kind.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    interface Whole{
        @Phone.Kind.Common<Unit>
        @Phone.Feature.Auth(["auth-basic"])
        suspend fun delete(id: Long): Any?

        @Phone.Kind.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    interface Multi{
        // TODO(is this allowed?)
        @Phone.Kind.Common<Int>
        @Phone.Feature.Auth(["auth-basic"], Phone.Feature.Auth.Strategy.Required)
        suspend fun get(): Any?

        @Phone.Kind.Common<Unit>
        suspend fun delete(id: Long): Any?

        @Phone.Kind.Common<User?>
        suspend fun search(id: Long): Any?
    }

    @Phone.Api
    @Phone.Feature.Auth(["auth-basic"])
    interface Jwt{
        @Phone.Feature.Auth(["auth-jwt"], withToken = true)
        @Phone.Kind.Common<Boolean>
        suspend fun delete(id: String): Any?
    }
}