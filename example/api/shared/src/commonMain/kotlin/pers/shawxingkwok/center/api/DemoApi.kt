package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.phone.Phone

@Phone.Api
interface DemoApi {
    @Phone.Call.Common<LoginResult>
    suspend fun login(id: Long, password: String): Any
}