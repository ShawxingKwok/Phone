package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    interface Partial{
        @Phone.Auth
        @Phone.Call.Common<Int>
        suspend fun delete(id: Int): Any?
    }

    @Phone.Auth
    @Phone.Api
    interface Whole{
        @Phone.Call.Common<Int>
        suspend fun delete(id: Int): Any?
    }

    @Phone.Api
    interface Multi{
        @Phone.Call.Common<Int>
        @Phone.Auth("auth-digest", "")
        suspend fun get(id: Int): Any?
    }

    @Phone.Api
    interface Jwt{
        @Phone.Auth("auth-jwt")
        @Phone.Call.Common<Int>
        suspend fun delete(id: Int): Any?
    }
}