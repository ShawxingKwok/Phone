package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    interface Partial{
        @Phone.Auth
        @Phone.Call.Common<Int>
        suspend fun delete(id: Int): Any
    }

    @Phone.Auth
    @Phone.Api
    interface Whole{
        @Phone.Auth("auth-digest")
        @Phone.Call.Common<Int>
        suspend fun delete(id: Int): Any
    }

    @Phone.Api
    interface Multi{
        @Phone.Call.Common<Int>
        @Phone.Auth("", "auth-digest")
        suspend fun delete(id: Int): Any
    }

    @Phone.Api
    interface Jwt{
        @Phone.Auth("auth-jwt")
        @Phone.Call.Common<Int>
        suspend fun delete(id: Int): Any
    }

    @Phone.Api
    interface Oauth{
        @Phone.Auth("auth-oauth")
        @Phone.Call.Common<Unit>(method = Phone.Method.Get)
        suspend fun login(): Any
    }
}