package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.server.phone.Phone

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    )
    .start(wait = true)
}

fun Application.module() {
    Phone.configure(routing{ }, ::AccountApiImpl, ::ChatApiImpl)
}

class AccountApiImpl(override val call: ApplicationCall) : Phone.AccountApi{
    private var fakeUser: User? = User(123456, "Shawxing", 25)

    override suspend fun login(email: String, password: String): LoginResult =
        when{
            password != "123456" -> LoginResult.PasswordWrong
            fakeUser == null -> LoginResult.NotSigned
            else -> LoginResult.Success(fakeUser!!)
        }

    override suspend fun delete(id: Long) {
        fakeUser = null
    }

    override suspend fun search(id: Long): User? = fakeUser
}

class ChatApiImpl(override val call: ApplicationCall) : Phone.ChatApi {
    override suspend fun getChats(): List<String> {
        return listOf("hello, world!")
    }
}