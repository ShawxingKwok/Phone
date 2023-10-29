package expect

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface TestApi {
    @Phone.Call.Common<User?>
    suspend fun search(id: String): Any?

    @Phone.Call.Common<Unit>
    suspend fun delete(id: String): Any

    @Phone.Call.WebSocket
    suspend fun get(i: Int = 1): Any

    @Phone.Call.WebSocket(true)
    suspend fun obtain(i: Int): Any

    @Phone.Call.Manual<Int?>
    suspend fun getFile(id: String): Any

    @Phone.Call.PartialContent<Int?>
    suspend fun getPartialFile(id: String): Any
}