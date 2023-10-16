package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Auth(["auth-basic"])
@Phone.Api
interface AccountApi {
    @Phone.Crypto
    suspend fun login(id: Long, password: String): LoginResult

    @Phone.Auth(["auth-bearer"], Phone.Auth.AuthenticationStrategy.Optional)
    suspend fun delete(id: Long)

    suspend fun search(id: Long): @Phone.Crypto User?
}