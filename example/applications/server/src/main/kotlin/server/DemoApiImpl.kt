package server

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.server.phone.*
import java.io.File

private val fakeUsers = mutableListOf(
    User(1, "William", "123456"),
    User(2, "Jack", "654321")
)

object DemoApiImpl : Phone.DemoApi {
    override suspend fun login(id: Long, password: String): Callback<LoginResult> =
    {
        val user = fakeUsers.firstOrNull { it.id == id }

        when{
            user == null -> LoginResult.NotSigned
            user.password == password -> LoginResult.Success(user)
            else -> LoginResult.PasswordWrong
        }
    }
}