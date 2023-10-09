package pers.shawxingkwok.phonesample.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.phonesample.model.LoginResult
import pers.shawxingkwok.phonesample.model.User

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