import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
import pers.shawxingkwok.center.Cipher
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.ktutil.allDo
import pers.shawxingkwok.test.server.*
import java.time.Duration
import java.util.*
import kotlin.test.Test

class PhoneTest {
    object JwtConfig {
        const val AUDIENCE = "jwt-audience"
        const val REALM = "ktor sample app"
        const val SECRET = "secret"
        const val ISSUER = "jwt issuer"
    }

    private fun start(
        configureServer: Application.() -> Unit = {},
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

                configureServer()
                // routePhone()
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

            phone.token = token

            act(phone)
        }

    @Test
    fun commonAccount() = start(
        configureServer = {
            Phone.route(routing {  }, AccountApiImpl)
        }
    ) { phone ->
        assert(phone.AccountApi().login("101", "", emptyList()).getOrThrow() == LoginResult.NotSigned)
        assert(phone.AccountApi().search(101).getOrThrow()?.id == 101L)
        phone.AccountApi().delete(0)
    }

    @Test
    fun crypto() = start(
        configureServer = {
            Phone.route(routing {  }, CryptoApiImpl.Partial)
            Phone.route(routing {  }, CryptoApiImpl.Whole)
        }
    ) { phone ->
        assert(phone.CryptoApi_Partial().getChats(1, "a", "b").getOrThrow() == listOf("1", "a", "b"))
        assert(phone.CryptoApi_Whole().getChats(1, "a").getOrThrow() == listOf("1", "a"))
    }

    @Test
    fun cipher() = start(
        configureServer = {
            Phone.route(routing {  }, AccountApiImpl)
        }
    ) { phone ->
        @Suppress("JsonStandardCompliance")
        var text = "hello"
        var bytes = Cipher.encrypt(text.encodeToByteArray())
        text = Json.encodeToString(ByteArraySerializer(), bytes)
        bytes = Json.decodeFromString(ByteArraySerializer(), text)
        bytes = Cipher.decrypt(bytes)
        text = bytes.decodeToString()
        assert(text == "hello")
    }

    @Test
    fun commonAuth() = start(
        configureServer = {
            Phone.route(routing {  }, AuthApiImpl.Partial)
            Phone.route(routing {  }, AuthApiImpl.Whole)
            Phone.route(routing {  }, AuthApiImpl.Multi)
        }
    ) { phone ->
        assert(phone.AuthApi_Partial().search(1).getOrThrow()?.id == 1L)
        phone.AuthApi_Partial().delete(1)
        println(".".repeat(10))

        assert(phone.AuthApi_Whole().search(1).getOrThrow()?.id == 1L)
        phone.AuthApi_Whole().delete(1)
        println(".".repeat(10))

        // assert(phone.authApi_Multi.get() == 1)
        assert(phone.AuthApi_Multi().search(1).getOrThrow()?.id == 1L)
        phone.AuthApi_Multi().delete(1)
    }

    @Test
    fun jwtAuth() = start(
        configureServer = {
            Phone.route(routing {  }, AuthApiImpl.Jwt)
        }
    ) { phone ->
        phone.AuthApi_Jwt().delete("f").getOrThrow()
    }

    @Test
    fun polymorphic() = start(
        configureServer = {
            Phone.route(routing {  }, PolymorphicApiImpl)
        }
    ) { phone ->
        assert(phone.PolymorphicApi().foo().getOrThrow() == "foo")
        assert(phone.PolymorphicApi().foo(1L).getOrThrow() == 1L)
        assert(phone.PolymorphicApi().foo(1).getOrThrow() == 1)
    }

    @Test
    fun `super`() = start(
        configureServer = {
            Phone.route(routing {  }, SuperInterfaceApiImpl)
        }
    ) { phone ->
        assert(phone.SuperInterfaceApi().foo().getOrThrow() == 1)
        assert(phone.SuperInterfaceApi().bar().getOrThrow() == 1)
    }

    @Test
    fun vararg() =  start(
        configureServer = {
            Phone.route(routing {  }, VarargApiImpl)
        }
    ) { phone ->
        assert(phone.VarargApi().sum(1, 2, 3).getOrThrow() == 6)
    }

    @Test
    fun customSerializer() = start(
        configureServer = {
            Phone.route(routing{ }, CustomSerializerApiImpl)
        }
    ) { phone ->
        val a = Time(1, 2, 3)
        val b = Time(4, 5, 6)
        val ab = phone.CustomSerializerApi().sumTime(a, b).getOrThrow()
        assert(ab == Time(5, 7, 9))
    }

    @Test
    fun file() = start(
        configureServer = {
            Phone.route(routing {  }, FileApiImpl)
        }
    ){phone ->
        phone.FileApi{
            setBody(byteArrayOf(1))
        }
        .exchange("122")
        .getOrThrow()
        .let { (headInfo, response) ->
            assert(headInfo == listOf("122")){
                headInfo
            }
            val bytes = response.readBytes()
            assert(bytes.contentEquals(byteArrayOf(1))){
                bytes.joinToString()
            }
        }

        phone.FileApi()
            .get("2")
            .getOrThrow()
            .let { (size, _) ->
                assert(size == null)
            }
    }

    @Test
    fun ws() = start(
        configureServer = {
            Phone.route(routing {  }, WebSocketApiImpl)
        }
    ){
        allDo(
            it,
            pers.shawxingkwok.test.client.Phone(
                client = it.client,
                host = "localhost",
                port = 80,
                withHttps = false,
                withWss = true,
            )
        ){ phone ->
            phone.WebSocketApi()
                .getSignals(1)
                .getOrThrow()
                .run {
                    assert((incoming.receive() as Frame.Text).readText() == "1")
                }

            phone.WebSocketApi()
                .getChats("1")
                .getOrThrow()
                .run {
                    assert((incoming.receive() as Frame.Text).readText() == "1")
                }
        }
    }
}