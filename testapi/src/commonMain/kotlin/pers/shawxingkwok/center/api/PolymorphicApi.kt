package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PolymorphicApi {
    @Phone.Common<String>
    suspend fun foo(): Any?

    @Phone.Common<Long>
    @Phone.Polymorphic("Long")
    suspend fun foo(i: Long): Any?

    @Phone.Common<Int>
    @Phone.Polymorphic("Int")
    suspend fun foo(j: Int): Any?
}