package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

object CryptoApi {
    @Phone.Api
    interface Partial {
        @Phone.Call.Common<List<String>>
        suspend fun getChats(
            @Phone.Crypto id: Long,
            name: @Phone.Crypto String,
            password: String,
        )
        : @Phone.Crypto Any?
    }

    @Phone.Api
    @Phone.Crypto
    interface Whole {
        @Phone.Call.Common<List<String>>
        suspend fun getChats(id: Long, name: String): Any?
    }
}