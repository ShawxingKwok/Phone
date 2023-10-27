package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface FileApi {
    @Phone.Kind.Manual<List<String>>
    suspend fun exchange(id: String): Any

    @Phone.Kind.Common<Int?>
    suspend fun get(id: String): Any

    // PartialContent
}