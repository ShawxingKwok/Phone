package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.model.LoginResult
import pers.shawxingkwok.test.model.User
import java.math.BigDecimal

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