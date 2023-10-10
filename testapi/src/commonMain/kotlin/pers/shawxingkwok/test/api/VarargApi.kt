package pers.shawxingkwok.test.api

import pers.shawxingkwok.phone.Phone

@Phone
interface VarargApi {
    suspend fun foo(vararg ints: Int)
    suspend fun bar(vararg str: String)
}