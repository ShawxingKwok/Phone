package server

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.server.phone.Phone
import pers.shawxingkwok.server.phone.PipelineContextProvider

object AccountApiImpl : Phone.AccountApi {
    var fakeUsers = mutableListOf(
        User(100, "Shawxing", 25),
        User(101, "Jack", 35)
    )

    override suspend fun login(id: Long, password: String): PipelineContextProvider<LoginResult> = {
        val user = fakeUsers.first { it.id == id }
        // verify password
        LoginResult.Success(user)
    }

    override suspend fun delete(id: Long): PipelineContextProvider<Unit> = {
        fakeUsers.removeIf { it.id == id }
    }

    override suspend fun search(id: Long): PipelineContextProvider<User?> = {
        fakeUsers.firstOrNull { it.id == id }.also {
            "on search: $it".let(::println)
        }
    }
}