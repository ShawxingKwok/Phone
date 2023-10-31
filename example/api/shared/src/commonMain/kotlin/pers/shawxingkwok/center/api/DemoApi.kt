package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.phone.Phone

@Phone.Api
interface DemoApi {
    @Phone.Call.Common<LoginResult>
    suspend fun login(id: String, @Phone.Crypto password: String): Any

    @Phone.Call.WebSocket
    suspend fun getChats(groupId: String): Any
}