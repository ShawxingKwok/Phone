package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface ManualApi {
    @Phone.Call.Manual<Long>
    suspend fun directGet(): Any

    @Phone.Call.Manual<List<String>>(method = Phone.Method.Post)
    suspend fun exchange(id: String): Any

    @Phone.Call.Manual<Int?>
    suspend fun getIdLength(id: String?): Any

    @Phone.Call.Manual<Unit>
    suspend fun getUnit(id: String): Any
}