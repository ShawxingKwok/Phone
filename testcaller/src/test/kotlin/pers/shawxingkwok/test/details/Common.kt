package pers.shawxingkwok.test.details

import org.junit.Assert.assertNull
import org.junit.Test
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone

class Common {
    object CommonApiImpl : Phone.CommonApi{
        override suspend fun login(id: Long, password: String): Callback<LoginResult> =
            {
                val user = User(id, "William", 10)
                LoginResult.Success(user)
            }

        override suspend fun delete(id: Long): Callback<Unit> = {

        }

        override suspend fun search(id: Long): Callback<User?> = {
            null
        }

        override suspend fun plus(time: Time?, hour: Int?, min: Int?, sec: Int): Callback<Time?> = {
            Time(hour!!, min!!, sec)
        }
    }

    @Test
    fun start() = testPhone(CommonApiImpl) { phone ->
        val api = phone.CommonApi()

        val ret = api.login(1, "1").getOrThrow()
        ret as LoginResult.Success
        assert(ret.user.id == 1L)

        api.delete(1).getOrThrow()

        assertNull(phone.CommonApi().search(1).getOrThrow())

        assert(api.plus(null, 1,1,1).getOrThrow() == Time(1,1,1))
    }
}