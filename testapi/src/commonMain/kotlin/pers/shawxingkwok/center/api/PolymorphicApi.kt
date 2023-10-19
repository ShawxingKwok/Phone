package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

@Phone.Api
interface PolymorphicApi {
    suspend fun foo(): String

    @Phone.Polymorphic("Long")
    suspend fun foo(i: Long): Long

    @Phone.Polymorphic("Int")
    suspend fun foo(j: Int): Int
}