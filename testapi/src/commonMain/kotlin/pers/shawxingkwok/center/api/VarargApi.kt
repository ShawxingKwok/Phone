package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface VarargApi {
    @Phone.Common<Int>
    suspend fun sum(vararg ints: Int): Any?
}