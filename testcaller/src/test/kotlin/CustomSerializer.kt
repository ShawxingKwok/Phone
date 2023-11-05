import org.junit.Test
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone

class CustomSerializer {
    object CustomSerializerApiImpl : Phone.CustomSerializerApi {
        override suspend fun sumTime(a: Time, b: Time): Callback<Time> = {
            Time(a.hour + b.hour, a.min + b.min, a.sec + b.sec)
        }
    }

    @Test
    fun start() = testPhone(CustomSerializerApiImpl){ phone ->
        val a = Time(1, 2, 3)
        val b = Time(4, 5, 6)
        val expect = Time(5, 7, 9)
        val ab = phone.CustomSerializerApi().sumTime(a, b).getOrThrow()
        assert(ab == expect)
    }
}