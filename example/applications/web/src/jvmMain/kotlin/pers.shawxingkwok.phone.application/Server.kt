package pers.shawxingkwok.phone.application

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import kotlinx.html.*
import java.time.Duration

// import pers.shawxingkwok.center.model.LoginResult
// import pers.shawxingkwok.center.model.User
// import pers.shawxingkwok.server.phone.Phone

fun main() {
    embeddedServer(Netty) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowHeader(HttpHeaders.Authorization)
            allowHeader("MyCustomHeader")
            anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
        }
        routing {
            // Phone.routeAll(this, ::AccountApiImpl, ::ChatApiImpl)
            webSocket("/echo") {
                send("Please enter your name")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    if (receivedText.equals("bye", ignoreCase = true)) {
                        // cancel("cancel")
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    } else {
                        send(Frame.Text("Hi, $receivedText!"))
                    }
                }
            }
            // I prefer putting it in resources.
            // However, it is different in a submodule.
            get("/") {
                call.respondHtml(HttpStatusCode.OK) {
                    body {
                        script(src = "/static/web.js") {}
                    }
                }
            }
            get("/X") {
                call.respondText("X")
            }
            staticResources("/static", null)
        }
    }.start(wait = true)
}

/*
class ChatApiImpl(override val context: PipelineContext<Unit, ApplicationCall>) : Phone.ChatApi {
    override suspend fun getChats(): List<String> {
        return listOf("Hello, world!")
    }
}

class AccountApiImpl(override val context: PipelineContext<Unit, ApplicationCall>) : Phone.AccountApi {
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
        fakeUsers.firstOrNull { it.id == id }.also {
            "on search: $it".let(::println)
        }
}*/
