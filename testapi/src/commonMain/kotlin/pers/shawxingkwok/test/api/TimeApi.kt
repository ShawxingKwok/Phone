package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.model.Time

@Phone.Api
interface TimeApi {
    suspend fun getTime(): Time
    suspend fun sumTime(vararg times: Time): Time
}