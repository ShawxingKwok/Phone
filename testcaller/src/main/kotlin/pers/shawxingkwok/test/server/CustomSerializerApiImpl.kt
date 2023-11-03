package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.util.pipeline.*
import pers.shawxingkwok.center.api.CustomSerializerApi
import pers.shawxingkwok.center.model.Time

object CustomSerializerApiImpl : Phone.CustomSerializerApi {
    override suspend fun sumTime(a: Time, b: Time): HttpResponser<Time> = {
        Time(a.hour + b.hour, a.min + b.min, a.sec + b.sec)
    }
}