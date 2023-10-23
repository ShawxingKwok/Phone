package pers.shawxingkwok.center.api

import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.phone.Phone

@Phone.Api
interface CustomSerializerApi {
    suspend fun sumTime(a: Time, b: Time): Time
}