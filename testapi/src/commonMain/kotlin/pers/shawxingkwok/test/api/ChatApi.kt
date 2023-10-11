package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
@Phone.Crypto
interface ChatApi {
    suspend fun getChats(): List<String>
}