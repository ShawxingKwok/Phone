package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PartialContentApi {
    @Phone.Kind.PartialContent<Pair<String, Long>>
    suspend fun partialGet(id: String): Any

    @Phone.Kind.PartialContent<Unit>
    suspend fun partialGetUnit(id: String): Any
}