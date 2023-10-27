package expect

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface TestApi {
    @Phone.Kind.Common<User?>
    suspend fun search(id: String): Any?

    @Phone.Kind.Common<Unit>
    suspend fun delete(id: String): Any

    @Phone.Kind.WebSocket
    suspend fun get(i: Int = 1): Any

    @Phone.Kind.WebSocket(true)
    suspend fun obtain(i: Int): Any

    @Phone.Kind.Manual<Int?>
    suspend fun getFile(id: String): Any

    @Phone.Kind.PartialContent<Int?>
    suspend fun getPartialFile(id: String): Any
}