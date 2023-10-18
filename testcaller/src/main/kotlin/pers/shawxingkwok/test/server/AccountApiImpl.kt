package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User

class AccountApiImpl(override val call: ApplicationCall) : Phone.AccountApi{
    override suspend fun login(email: String, password: String): LoginResult {
        return LoginResult.NotSigned
    }

    override suspend fun delete(id: Long) {

    }

    override suspend fun search(id: Long): User? {
        return User(id, "S", 25)
    }
}