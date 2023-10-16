package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
@Phone.Auth
interface AccountApi {
    suspend fun login(
        email: String,
        password: String,
    )
    : LoginResult

    @Phone.Auth(["auth-bearer"])
    suspend fun delete(id: Long)

    suspend fun search(id: Long): User?
}