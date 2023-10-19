import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.json.Json
import org.apache.http.impl.auth.BasicScheme.authenticate
import pers.shawxingkwok.center.Cipher
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.test.server.*
import java.time.Duration
import kotlin.reflect.KFunction
import kotlin.test.Test

class ApplicationTest {
    private fun Application.routePhone(){
        Phone.route(
            route = routing { },
            ::AccountApiImpl,
            AuthApiImpl::Partial,
            AuthApiImpl::Whole,
            AuthApiImpl::Multi,
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
                }

                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                    timeout = Duration.ofSeconds(15)
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }

                routePhone()
            }

            val client = createClient{
                configureClient()
                install(io.ktor.client.plugins.websocket.WebSockets)
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = "jetbrains", password = "foobar")
                        }
                        realm = "Access to the '/' path"
                    }

                    bearer {
                        loadTokens {
                            // Load tokens from a local storage and return them as the 'BearerTokens' instance
                            BearerTokens("abc123", "xyz111")
                        }
                    }
                }
            }
            val phone = pers.shawxingkwok.test.client.Phone(client)
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
    fun auth() = start { phone ->
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
        phone.MyWebSocketWithArgs{ session ->
            val textFrame = session.incoming.receive() as Frame.Text
            assert(textFrame.readText() == "1 a")
        }
        .getChats(1, "a")

        phone.MyWebSocketWithAuth(::connect).getChats()
    }

    @Test
    fun myAuthWebSocket() = testApplication{
        application {
            routing {
                authenticate {
                    webSocket("/") {

                    }
                }
            }
        }

        val client = createClient {
            install(io.ktor.client.plugins.websocket.WebSockets)
            install(Auth){
                // basic {
                //     realm = "Access to the '/' path"
                //     credentials {
                //         println(".".repeat(20))
                //         BasicAuthCredentials(username = "jetbrains", password = "foobar")
                //     }
                // }
                bearer {
                    loadTokens {
                        // Load tokens from a local storage and return them as the 'BearerTokens' instance
                        BearerTokens("abc123", "xyz111")
                    }
                }
            }
        }

        client.webSocket("/") {

        }
    }
}