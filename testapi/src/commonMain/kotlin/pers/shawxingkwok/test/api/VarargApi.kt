package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone
import pers.shawxingkwok.test.model.Time

@Phone
interface VarargApi {
    suspend fun foo(vararg ints: Int)
    suspend fun bar(vararg str: String)
    suspend fun baz(vararg times: Time)
}