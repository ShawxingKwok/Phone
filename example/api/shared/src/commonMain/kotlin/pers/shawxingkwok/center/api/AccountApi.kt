package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    suspend fun login(id: Long, password: String): LoginResult

    suspend fun delete(id: Long)

    suspend fun search(id: Long): User?
}