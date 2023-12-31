package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

interface Super{
    @Phone.Call.Common<Int>
    suspend fun foo(): Any
}

@Phone.Api
interface SuperInterfaceApi : Super{
    @Phone.Call.Common<Int>
    suspend fun bar(): Any
}