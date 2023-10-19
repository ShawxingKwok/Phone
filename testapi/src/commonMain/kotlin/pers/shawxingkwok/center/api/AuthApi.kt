package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

@Phone.Api
interface AuthApi {
    @Phone.Auth(["auth-bearer"])
    suspend fun delete(id: Long)

    suspend fun search(id: Long): User?
}