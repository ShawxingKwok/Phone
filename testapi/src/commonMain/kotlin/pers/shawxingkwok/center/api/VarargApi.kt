package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface VarargApi {
    suspend fun sum(vararg ints: Int): Int
}