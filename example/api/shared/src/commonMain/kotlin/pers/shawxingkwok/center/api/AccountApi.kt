package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    @Phone.Common<LoginResult>
    suspend fun login(id: Long, password: String): Any?

    @Phone.Common<Unit>
    suspend fun delete(id: Long): Any?

    @Phone.Common<User?>
    suspend fun search(id: Long): Any?
}