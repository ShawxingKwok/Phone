package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

object AuthApi {
    @Phone.Api
    @Phone.Auth(["auth-basic"])
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
}