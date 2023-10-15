package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    @Phone.Auth(["auth-bearer"], Phone.Auth.AuthenticationStrategy.Optional)
    @Phone.Crypto
    suspend fun login(id: Long, password: String): LoginResult

    suspend fun delete(id: Long)

    @Phone.Auth(["auth-basic"])
    suspend fun search(id: Long): @Phone.Crypto User?
}