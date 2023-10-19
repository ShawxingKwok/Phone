package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.center.model.Time

@Phone.Api
interface VarargApi {
    suspend fun sumTime(vararg ints: Int): Int
}