package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.center.model.User

@Phone.Api
interface CommonApi {
    @Phone.Call.Common<LoginResult>
    suspend fun login(id: Long, password: String): Any

    @Phone.Call.Common<Unit>
    suspend fun delete(id: Long): Any

    @Phone.Call.Common<User?>(method = Phone.Method.Get)
    suspend fun search(id: Long): Any

    @Phone.Call.Common<Time?>
    suspend fun plus(time: Time?, hour: Int?, min: Int?, sec: Int): Any
}