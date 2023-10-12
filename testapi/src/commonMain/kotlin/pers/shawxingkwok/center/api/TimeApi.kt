package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.Time

@Phone.Api
interface TimeApi {
    suspend fun getTime(): Time
    suspend fun sumTime(vararg times: Time): Time
}