package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PolymorphicApi {
    suspend fun foo()

    @Phone.Polymorphic("Long")
    suspend fun foo(i: Long)

    @Phone.Polymorphic("Int")
    suspend fun foo(j: Int)
}