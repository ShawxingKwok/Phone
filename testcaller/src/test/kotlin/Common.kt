import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import kotlin.test.assertNull

class Common {
    object CommonApiImpl : Phone.AccountApi{
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
    }

    @Test
    fun start() = testPhone(CommonApiImpl){phone ->
        val api = phone.AccountApi()

        val ret = api.login(1, "1").getOrThrow()
        ret as LoginResult.Success
        assert(ret.user.id == 1L)

        api.delete(1).getOrThrow()

        assertNull(phone.AccountApi().search(1).getOrThrow())
    }
}