package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    @Phone.Crypto
    suspend fun login(
        email: String,
        password: String,
    )
    : LoginResult

    suspend fun delete(id: Long)

    suspend fun delete(id: String)

    suspend fun search(id: Long): @Phone.Crypto User?
}