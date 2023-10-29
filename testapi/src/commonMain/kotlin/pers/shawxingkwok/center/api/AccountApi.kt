package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    @Phone.Call.Common<LoginResult>
    suspend fun login(email: String, password: String, code: List<String>): Any?

    @Phone.Call.Common<Unit>
    suspend fun delete(id: Long): Any?

    @Phone.Call.Common<User?>(method = Phone.Method.Get)
    suspend fun search(id: Long): Any?
}