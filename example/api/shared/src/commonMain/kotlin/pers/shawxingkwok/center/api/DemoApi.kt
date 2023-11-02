package pers.shawxingkwok.center.api

import kotlinx.serialization.Serializable
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

@Phone.Api(Phone.Method.Get)
interface DemoApi {
    @Phone.Call.Common<LoginResult>(Phone.Method.Post)
    suspend fun login(id: Long, password: String): Any

    @Phone.Call.Common<User?>
    suspend fun search(id: Long): Any
}