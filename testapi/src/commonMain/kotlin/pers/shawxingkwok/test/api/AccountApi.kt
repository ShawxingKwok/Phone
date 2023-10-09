package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.model.LoginResult
import pers.shawxingkwok.test.model.User

@Phone
interface AccountApi {
    suspend fun login(
        email: String,
        password: String,
        verificationCode: String? = null,
    )
    : LoginResult

    suspend fun delete(id: Long)

    suspend fun search(id: Long): User?
}