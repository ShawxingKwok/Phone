package pers.shawxingkwok.phonesample.api

import pers.shawxingkwok.phone.Phone

@Phone
interface ChatApi {
    suspend fun getChats(): List<String>
}