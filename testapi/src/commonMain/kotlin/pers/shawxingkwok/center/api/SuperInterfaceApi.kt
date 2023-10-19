package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

interface Super{
    suspend fun foo(): Int
}

@Phone.Api
interface SuperInterfaceApi : Super{
    suspend fun bar(): Int
}