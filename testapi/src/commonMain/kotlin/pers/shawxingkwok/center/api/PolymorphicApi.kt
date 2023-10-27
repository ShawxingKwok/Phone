package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PolymorphicApi {
    @Phone.Kind.Common<String>
    suspend fun foo(): Any?

    @Phone.Kind.Common<Long>
    @Phone.Feature.Polymorphic("Long")
    suspend fun foo(i: Long): Any?

    @Phone.Kind.Common<Int>
    @Phone.Feature.Polymorphic("Int")
    suspend fun foo(j: Int): Any?
}