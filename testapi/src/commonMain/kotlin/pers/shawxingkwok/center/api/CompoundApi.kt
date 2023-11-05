package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.phone.Phone

@Phone.Crypto
@Phone.Auth
@Phone.Api(defaultMethod = Phone.Method.Post)
interface CompoundApi {
    @Phone.Call.Common<Time>(method = Phone.Method.Get)
    suspend fun foo(i: Int): Any

    @Phone.Call.Manual<Time>(method = Phone.Method.Get, polymorphicId = "manual")
    suspend fun foo(i: Long): Any

    @Phone.Call.PartialContent<Time>(polymorphicId = "partial content")
    suspend fun foo(i: Byte): Any

    @Phone.Call.WebSocket(polymorphicId = "websocket")
    suspend fun foo(i: Short): Any
}