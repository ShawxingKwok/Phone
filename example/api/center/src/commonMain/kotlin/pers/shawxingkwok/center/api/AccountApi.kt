package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    suspend fun login(
        email: String,
        password: String,
    )
    : LoginResult

    suspend fun delete(id: Long)

    suspend fun search(id: Long): @Phone.Crypto User?
}