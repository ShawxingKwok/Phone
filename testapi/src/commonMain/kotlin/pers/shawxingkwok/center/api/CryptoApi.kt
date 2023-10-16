package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface CryptoApi {
    suspend fun get(): @Phone.Crypto List<String>
}