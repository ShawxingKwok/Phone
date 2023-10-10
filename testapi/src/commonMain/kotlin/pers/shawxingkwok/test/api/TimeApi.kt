package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.model.Time

@Phone
interface TimeApi {
    suspend fun getTime(): Time
    suspend fun sumTime(a: Time, b: Time): Time
}