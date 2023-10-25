package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.pipeline.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

object AccountApiImpl : Phone.AccountApi{
    override suspend fun login(email: String, password: String, code: List<String>): CommonConnector<LoginResult> =
    {
        LoginResult.NotSigned
    }

    override suspend fun delete(id: Long): CommonConnector<Unit> = {

    }

    override suspend fun search(id: Long): CommonConnector<User?> = {
        User(id, "William", 21)
    }
}