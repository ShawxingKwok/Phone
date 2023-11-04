package pers.shawxingkwok.center.api

import kotlinx.serialization.Serializable
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.phone.Phone

@Phone.Api
interface DemoApi {
    @Phone.Call.Common<LoginResult>
    suspend fun login(id: Long, password: String): Any

    @Phone.Call.Common<User?>
    suspend fun search(id: Long): Any

    @Phone.Call.Common<Unit>
    suspend fun uploadFile(name: String, length: Long, type: String?): Any

    @Phone.Call.Manual<Pair<String, Long>>
    suspend fun downloadFile(path: String): Any

    @Phone.Call.PartialContent<Pair<String, Long>>
    suspend fun downloadBigFile(path: String): Any

    @Phone.Call.WebSocket(isRaw = true)
    suspend fun getChats(groupId: Long): Any
}