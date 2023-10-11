package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.model.LoginResult
import pers.shawxingkwok.test.model.User
import java.math.BigDecimal

@Phone.Api
interface AccountApi {
    suspend fun login(
        email: String,
        @Phone.Crypto password: String,
    )
    : LoginResult

    suspend fun delete(id: Long)

    @Phone.Crypto
    suspend fun search(id: Long): User?
}