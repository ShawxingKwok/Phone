package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.routing.*
import pers.shawxingkwok.test.model.LoginResult
import pers.shawxingkwok.test.model.Time
import pers.shawxingkwok.test.model.User

class AccountApiImpl(call: ApplicationCall) : Phone.AccountApi(call){
    override suspend fun login(email: String, password: String, verificationCode: String?): LoginResult {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun search(id: Long): User? {
        TODO("Not yet implemented")
    }
}

class ChatApiImpl(call: ApplicationCall) : Phone.ChatApi(call){
    override suspend fun getChats(): List<String> {
        TODO("Not yet implemented")
    }
}

class TimeApiImpl(call: ApplicationCall) : Phone.TimeApi(call){
    override suspend fun getTime(): Time {
        TODO("Not yet implemented")
    }

    override suspend fun sumTime(a: Time, b: Time): Time {
        TODO("Not yet implemented")
    }
}

fun Application.main() {
    routing {
        Phone.configure(this, ::AccountApiImpl, ::ChatApiImpl, :: TimeApiImpl)
    }
}