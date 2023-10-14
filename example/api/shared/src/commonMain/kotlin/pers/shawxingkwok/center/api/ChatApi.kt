package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface ChatApi {
    suspend fun getChats(): List<String>
}