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
    @Phone.Auth(["auth-bearer"])
    interface Whole{
        @Phone.Auth(["auth-basic"])
        suspend fun delete(id: Long)
        suspend fun search(id: Long): User?
    }

    @Phone.Api
    @Phone.Auth(["auth-bearer", "auth-basic"], strategy = Phone.AuthenticationStrategy.Required)
    interface Multi{
        suspend fun delete(id: Long)
        suspend fun search(id: Long): User?
    }
}