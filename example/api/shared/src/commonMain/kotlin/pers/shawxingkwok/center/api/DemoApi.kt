package pers.shawxingkwok.center.api

import kotlinx.serialization.Serializable
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

@Phone.Crypto
@Phone.Api
interface DemoApi {
    @Phone.Crypto
    @Phone.Call.Common<LoginResult>
    suspend fun login(@Phone.Crypto id: Long, password: @Phone.Crypto String): @Phone.Crypto Any

    @Phone.Call.Common<Unit>
    suspend fun uploadFile(name: String, length: Long, type: String?): Any

    @Phone.Call.Manual<Pair<String, Long>>
    suspend fun downloadFile(path: String): Any

    @Phone.Call.PartialContent<Pair<String, Long>>
    suspend fun downloadBigFile(path: String): Any

    @Phone.Call.WebSocket
    suspend fun getChats(groupId: Long): Any
}