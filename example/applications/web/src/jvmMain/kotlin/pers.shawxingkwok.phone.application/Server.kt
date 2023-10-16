package pers.shawxingkwok.phone.application

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.Netty
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.html.*
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.server.phone.Phone
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            Phone.configure(this, ::AccountApiImpl, ::ChatApiImpl)

            // I prefer putting it in resources.
            // However, it is different in a submodule.
            get("/") {
                call.respondHtml(HttpStatusCode.OK) {
                    body {
                        script(src = "/static/web.js") {}
                    }
                }
            }
            staticResources("/static", null)
        }
    }.start(wait = true)
}

class ChatApiImpl(override val call: ApplicationCall) : Phone.ChatApi {
    override suspend fun getChats(): List<String> {
        return listOf("Hello, world!")
    }
}

class AccountApiImpl(override val call: ApplicationCall) : Phone.AccountApi {
    var fakeUsers = mutableListOf(
        User(100, "Shawxing", 25),
        User(101, "Jack", 35)
    )

    override suspend fun login(id: Long, password: String): LoginResult {
        val user = search(id) ?: return LoginResult.NotSigned
        // if(!testPassword) return LoginResult.PasswordWrong
        return LoginResult.Success(user)
    }

    override suspend fun delete(id: Long) {
        fakeUsers.removeIf { it.id == id }
    }

    override suspend fun search(id: Long): User? =
        fakeUsers.firstOrNull { it.id == id }
}