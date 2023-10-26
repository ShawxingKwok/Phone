package expect

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface TestApi {
    @Phone.Common<User?>
    suspend fun search(id: String): Any?

    @Phone.Common<Unit>
    suspend fun delete(id: String): Any

    @Phone.WebSocket
    suspend fun get(i: Int = 1): Any

    @Phone.WebSocket(true)
    suspend fun obtain(i: Int): Any

    @Phone.File<Int>
    suspend fun getFile(id: String): Any
}