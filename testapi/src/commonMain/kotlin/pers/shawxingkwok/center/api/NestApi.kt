package pers.shawxingkwok.center.api

import pers.shawxingkwok.phone.Phone

object NestApi {
    @Phone.Api
    interface XApi{
        suspend fun foo()
    }
}