import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.cio.*
import junit.framework.TestCase.assertEquals
import org.apache.http.protocol.ResponseContent
import org.junit.Test
import java.io.File

class OtherTest {
    @Test
    fun session() = testApplication {
        application {
            main()
        }

        val client = createClient {
            install(HttpCookies)
        }

        val loginResponse = client.post("/login") {
            // header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            val params = parameters {
                append("username", "jetbrains")
                append("password", "foobar")
            }
            setBody(FormDataContent(params))
        }
        assertEquals("user_session", loginResponse.setCookie()[0].name)
    }

    @Test
    fun foo() = testApplication {
    }
}

data class UserSession(val name: String, val count: Int) : Principal

fun Application.main() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60
        }
    }
    install(Authentication) {
        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == "jetbrains" && credentials.password == "foobar") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
        session<UserSession>("auth-session") {
            validate { session ->
                if(session.name.startsWith("jet")) {
                    session
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }

    routing {
        get("/login") {
            call.respondHtml {
                body {
                    form(action = "/login", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                        p {
                            +"Username:"
                            textInput(name = "username")
                        }
                        p {
                            +"Password:"
                            passwordInput(name = "password")
                        }
                        p {
                            submitInput() { value = "Login" }
                        }
                    }
                }
            }
        }

        authenticate("auth-form") {
            post("/login") {
                val userName = call.principal<UserIdPrincipal>()?.name.toString()
                call.sessions.set(UserSession(name = userName, count = 1))
                call.respondRedirect("/hello")
            }
        }

        authenticate("auth-session") {
            get("/hello") {
                val userSession = call.principal<UserSession>()
                call.sessions.set(userSession?.copy(count = userSession.count + 1))
                call.respondText("Hello, ${userSession?.name}! Visit count is ${userSession?.count}.")
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/login")
        }
    }
}