package server

import io.ktor.server.application.*
import io.ktor.server.response.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.server.phone.HttpResponser
import pers.shawxingkwok.server.phone.Phone
import pers.shawxingkwok.server.phone.WebSocketConnector
import java.io.File

private val fakeUsers = mutableListOf(
    User(1, "William", "123456"),
    User(2, "Jack", "654321")
)

object DemoApiImpl : Phone.DemoApi {
    override suspend fun login(id: Long, password: String): HttpResponser<LoginResult> =
    {
        val user = fakeUsers.firstOrNull { it.id == id }

        when{
            user == null -> LoginResult.NotSigned
            user.password == password -> LoginResult.Success(user)
            else -> LoginResult.PasswordWrong
        }
    }

    override suspend fun uploadFile(
        name: String,
        length: Long,
        type: String?
    )
        : HttpResponser<Unit> =
    {

    }

    @Suppress("UNREACHABLE_CODE")
    override suspend fun downloadFile(path: String): HttpResponser<Pair<String, Long>> =
    {
        val file: File = TODO("search with path")
        call.respondFile(file) // or with stream or channel

        val type = file.name.substringAfterLast(".")
        val length = file.length()

        type to length
    }

    override suspend fun downloadBigFile(path: String): HttpResponser<Pair<String, Long>> {
        TODO("Not yet implemented")
    }

    override suspend fun getChats(groupId: Long): WebSocketConnector =
    {

    }
}