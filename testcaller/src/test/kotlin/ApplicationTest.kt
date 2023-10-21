import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.json.Json
import org.apache.http.client.methods.HttpHead
import pers.shawxingkwok.center.Cipher
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.test.server.*
import java.time.Duration
import java.util.*
import kotlin.test.Test

class ApplicationTest {
    private fun Application.routePhone() {
        Phone.routeAll(
            route = routing { },
            ::AccountApiImpl,
            AuthApiImpl::Partial,
            AuthApiImpl::Whole,
            AuthApiImpl::Multi,
            AuthApiImpl::Jwt,
            CryptoApiImpl::Partial,
            CryptoApiImpl::Whole,
            ::PolymorphicApiImpl,
            ::SuperInterfaceApiImpl,
            ::VarargApiImpl,
            ::MyWebSocketImpl,
            ::MyRawWebSocketImpl,
            ::MySubProtocolWebSocketImpl,
            ::MyWebSocketWithAuthImpl,
            ::MyWebSocketWithArgsImpl,
        )
    }

    object JwtConfig {
        const val AUDIENCE = "jwt-audience"
        const val REALM = "ktor sample app"
        const val SECRET = "secret"
        const val ISSUER = "jwt issuer"
    }

    private fun start(
        configureClient: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit = {},
        act: suspend ApplicationTestBuilder.(pers.shawxingkwok.test.client.Phone) -> Unit,
    ) =
        testApplication {
            application {
                install(Authentication) {
                    basic {
                        realm = "Access to the '/' path"
                        validate { credentials ->
                            if (credentials.name == "jetbrains" && credentials.password == "foobar") {
                                UserIdPrincipal(credentials.name)
                            } else {
                                null
                            }
                        }
                    }
                    basic("auth-basic") {
                        realm = "Access to the '/' path"
                        validate { credentials ->
                            if (credentials.name == "jetbrains" && credentials.password == "foobar") {
                                UserIdPrincipal(credentials.name)
                            } else {
                                null
                            }
                        }
                    }
                    bearer("auth-bearer") {
                        realm = "Access to the '/' path"
                        authenticate { tokenCredential ->
                            if (tokenCredential.token == "abc123") {
                                UserIdPrincipal("jetbrains")
                            } else {
                                null
                            }
                        }
                    }

                    jwt("auth-jwt") {
                        realm = JwtConfig.REALM

                        JWT.require(Algorithm.HMAC256(JwtConfig.SECRET))
                            .withAudience(JwtConfig.AUDIENCE)
                            .withIssuer(JwtConfig.ISSUER)
                            .build()
                            .let(::verifier)

                        validate { credential ->
                            if (credential.payload.getClaim("username").asString() == "shawxing") {
                                JWTPrincipal(credential.payload)
                            } else {
                                null
                            }
                        }
                        challenge { defaultScheme, realm ->
                            println("115: $defaultScheme $realm ${this.call.request.header(HttpHeaders.Authorization)}")
                            call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
                        }
                    }
                }

                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                    timeout = Duration.ofSeconds(15)
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }

                routing {
                    post("/login") {
                        val username = call.receiveText()
                        // Check username and password
                        // ...
                        val token = JWT.create()
                            .withAudience(JwtConfig.AUDIENCE)
                            .withIssuer(JwtConfig.ISSUER)
                            .withClaim("username", username)
                            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                            .sign(Algorithm.HMAC256(JwtConfig.SECRET))

                        call.respondText(token)
                    }
                }

                routePhone()
            }

            val client = createClient {
                configureClient()
                install(io.ktor.client.plugins.websocket.WebSockets)
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = "jetbrains", password = "foobar")
                        }
                        realm = "Access to the '/' path"
                    }
                }
            }

            val phone = pers.shawxingkwok.test.client.Phone(client)

            val token = phone.client
                .post("login") { setBody("shawxing") }
                .bodyAsText()

            phone.setAuthorization(token)

            act(phone)
        }

    @Test
    fun commonAccount() = start { phone ->
        assert(phone.accountApi.login("101", "") == LoginResult.NotSigned)
        assert(phone.accountApi.search(101)?.id == 101L)
        phone.accountApi.delete(0)
    }

    @Test
    fun crypto() = start { phone ->
        assert(phone.cryptoApi_Partial.getChats(1, "a", "b") == listOf("1", "a", "b"))
        assert(phone.cryptoApi_Whole.getChats(1, "a") == listOf("1", "a"))
    }

    @Test
    fun cipher() {
        var text = "hello"
        var bytes = Cipher.encrypt(text.encodeToByteArray())
        text = Json.encodeToString(ByteArraySerializer(), bytes)
        bytes = Json.decodeFromString(ByteArraySerializer(), text)
        bytes = Cipher.decrypt(bytes)
        text = bytes.decodeToString()
        assert(text == "hello")
    }

    @Test
    fun commonAuth() = start { phone ->
        assert(phone.authApi_Partial.search(1)?.id == 1L)
        phone.authApi_Partial.delete(1)
        println(".".repeat(10))

        assert(phone.authApi_Whole.search(1)?.id == 1L)
        phone.authApi_Whole.delete(1)
        println(".".repeat(10))

        // assert(phone.authApi_Multi.get() == 1)
        assert(phone.authApi_Multi.search(1)?.id == 1L)
        phone.authApi_Multi.delete(1)
    }

    @Test
    fun jwtAuth() = start{phone ->
        assert(phone.authApi_Jwt.delete("f"))

        phone.MyWebSocketWithAuth {
            assert((it.incoming.receive() as Frame.Text).readText() == "hello, world!")
        }
        .getChats()
    }

    @Test
    fun polymorphic() = start { phone ->
        assert(phone.polymorphicApi.foo() == "foo")
        assert(phone.polymorphicApi.foo(1L) == 1L)
        assert(phone.polymorphicApi.foo(1) == 1)
    }

    @Test
    fun `super`() = start { phone ->
        assert(phone.superInterfaceApi.foo() == 1)
        assert(phone.superInterfaceApi.bar() == 1)
    }

    @Test
    fun vararg() = start { phone ->
        assert(phone.varargApi.sumTime(1, 2, 3) == 6)
    }

    @Test
    fun websocket() = start { phone ->
        suspend fun connect(session: ClientWebSocketSession) {
            val textFrame = session.incoming.receive() as Frame.Text
            assert(textFrame.readText() == "hello, world!")
        }

        phone.MyWebSocket(::connect).getChats()
        phone.MyRawWebSocket(::connect).getChats()
        phone.MySubProtocolWebSocket(::connect).getChats()
        phone.MyWebSocketWithArgs { session ->
            val textFrame = session.incoming.receive() as Frame.Text
            assert(textFrame.readText() == "1 a")
        }
        .getChats(1, "a")

        phone.MyWebSocketWithAuth(::connect).getChats()
    }
}