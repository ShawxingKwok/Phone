package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PartialContentApi {
    @Phone.Call.PartialContent<Pair<String, Long>>
    suspend fun partialGet(id: String): Any

    @Phone.Call.PartialContent<Unit>
    suspend fun partialGetUnit(id: String): Any
}