package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface ManualApi {
    @Phone.Kind.Manual<Long>
    suspend fun directGet(): Any

    @Phone.Method.Post(false)
    @Phone.Kind.Manual<List<String>>
    suspend fun exchange(id: String): Any

    @Phone.Kind.Manual<Int?>
    suspend fun getIdLength(id: String?): Any

    @Phone.Kind.Manual<Unit>
    suspend fun getUnit(id: String): Any
}