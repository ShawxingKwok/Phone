package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone

@Phone
interface ChatApi {
    suspend fun getChats(): List<String>
}