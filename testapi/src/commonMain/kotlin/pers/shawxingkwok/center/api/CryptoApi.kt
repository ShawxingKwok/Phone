package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

object CryptoApi {
    @Phone.Api
    interface Partial {
        suspend fun getChats(
            @Phone.Crypto id: Long,
            name: @Phone.Crypto String,
            password: String,
        )
        : @Phone.Crypto List<String>
    }

    @Phone.Api
    @Phone.Crypto
    interface Whole {
        suspend fun getChats(id: Long, name: String): List<String>
    }
}