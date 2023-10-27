package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

object CryptoApi {
    @Phone.Api
    interface Partial {
        @Phone.Kind.Common<List<String>>
        suspend fun getChats(
            @Phone.Feature.Crypto id: Long,
            name: @Phone.Feature.Crypto String,
            password: String,
        )
        : @Phone.Feature.Crypto Any?
    }

    @Phone.Api
    @Phone.Feature.Crypto
    interface Whole {
        @Phone.Kind.Common<List<String>>
        suspend fun getChats(id: Long, name: String): Any?
    }
}