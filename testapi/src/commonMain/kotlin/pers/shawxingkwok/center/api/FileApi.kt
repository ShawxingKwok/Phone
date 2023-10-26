package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface FileApi {
    @Phone.File<List<String>>
    suspend fun exchange(id: String): Any

    @Phone.File<Int?>
    suspend fun get(id: String): Any
}