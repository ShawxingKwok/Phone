package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PolymorphicApi {
    @Phone.Call.Common<String>
    suspend fun foo(): Any?

    @Phone.Call.Common<Long>(polymorphicId = "Long")
    suspend fun foo(i: Long): Any?

    @Phone.Call.Common<Int>(polymorphicId = "Int")
    suspend fun foo(j: Int): Any?
}