package pers.shawxingkwok.test.server

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.center.model.User

class AccountApiImpl(override val call: ApplicationCall) : Phone.AccountApi{
    override suspend fun login(email: String, password: String): LoginResult {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun search(id: Long): User? {
        TODO("Not yet implemented")
    }
}

class ChatApiImpl(override val call: ApplicationCall) : Phone.ChatApi{
    override suspend fun getChats(): List<String> {
        TODO("Not yet implemented")
    }
}

class TimeApiImpl(override val call: ApplicationCall) : Phone.TimeApi{
    override suspend fun getTime(): Time {
        TODO("Not yet implemented")
    }

    override suspend fun sumTime(vararg times: Time): Time {
        TODO("Not yet implemented")
    }
}

fun Application.main() {
    routing {
        get {
            call.request.contentType()

        }

        Phone.configure(this, ::AccountApiImpl, ::ChatApiImpl, ::TimeApiImpl)
    }
}