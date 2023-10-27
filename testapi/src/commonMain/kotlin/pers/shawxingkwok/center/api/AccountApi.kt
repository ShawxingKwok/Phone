package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

@Phone.Api
interface AccountApi {
    @Phone.Kind.Common<LoginResult>
    suspend fun login(email: String, password: String, code: List<String>): Any?

    @Phone.Kind.Common<Unit>
    suspend fun delete(id: Long): Any?

    @Phone.Kind.Common<User?>
    suspend fun search(id: Long): Any?
}