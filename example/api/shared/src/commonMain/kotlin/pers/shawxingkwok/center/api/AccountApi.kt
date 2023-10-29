package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    @Phone.Call.Common<LoginResult>
    suspend fun login(id: Long, password: String): Any

    @Phone.Call.Common<Unit>
    suspend fun delete(id: Long): Any

    @Phone.Call.Common<User?>
    suspend fun search(id: Long): Any?
}