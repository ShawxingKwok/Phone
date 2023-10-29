package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface VarargApi {
    @Phone.Call.Common<Int>
    suspend fun sum(vararg ints: Int): Any?
}