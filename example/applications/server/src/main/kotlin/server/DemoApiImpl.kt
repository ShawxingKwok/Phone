package server

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.server.phone.Phone
import pers.shawxingkwok.server.phone.PipelineContextProvider

private val fakeUsers = mutableListOf(
    User(1, "William", "123456"),
    User(2, "Jack", "654321")
)

object DemoApiImpl : Phone.DemoApi {
    override suspend fun login(id: Long, password: String): PipelineContextProvider<LoginResult> = {
        val user = fakeUsers.firstOrNull { it.id == id }

        when{
            user == null -> LoginResult.NotSigned
            user.password == password -> LoginResult.Success(user)
            else -> LoginResult.PasswordWrong
        }
    }
}