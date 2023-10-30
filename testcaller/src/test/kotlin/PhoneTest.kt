import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.json.Json
import pers.shawxingkwok.center.Cipher
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.test.server.*
import java.io.File
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNull

class PhoneTest {
    private fun start(
        configureServer: Application.() -> Unit = {},
        enables: Boolean = false,
        configureClient: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit = {},
        requestOnClient: suspend ApplicationTestBuilder.(pers.shawxingkwok.test.client.Phone) -> Unit,
    ) =
        testApplication {
            application{
                installPlugins()
                configureServer()
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

            val token = JWT.create()
                .withAudience(JwtConfig.AUDIENCE)
                .withIssuer(JwtConfig.ISSUER)
                .withClaim("username", "shawxing")
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256(JwtConfig.SECRET))

            val phone = pers.shawxingkwok.test.client.Phone(client, enablesWss = enables, token = token)

            requestOnClient(phone)
        }

    @Test
    fun commonAccount() = start(
        configureServer = {
            Phone.route(routing { }, AccountApiImpl)
        }
    ) { phone ->
        assert(phone.AccountApi().login("101", "", emptyList()).getOrThrow() == LoginResult.NotSigned)
        assert(phone.AccountApi().search(101).getOrThrow()?.id == 101L)
        phone.AccountApi().delete(0)
    }

    @Test
    fun crypto() = start(
        configureServer = {
            Phone.route(routing { }, CryptoApiImpl.Partial)
            Phone.route(routing { }, CryptoApiImpl.Whole)
        }
    ) { phone ->
        assert(phone.CryptoApi_Partial().getChats(1, "a", "b").getOrThrow() == listOf("1", "a", "b"))
        assert(phone.CryptoApi_Whole().getChats(1, "a").getOrThrow() == listOf("1", "a"))
    }

    @Test
    fun cipher() {
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
            Phone.route(routing { }, AuthApiImpl.Partial)
            Phone.route(routing { }, AuthApiImpl.Whole)
            Phone.route(routing { }, AuthApiImpl.Multi)
        }
    ) { phone ->
        assert(phone.AuthApi_Partial().search(1).getOrThrow()?.id == 1L)
        phone.AuthApi_Partial().delete(1)

        assert(phone.AuthApi_Whole().search(1).getOrThrow()?.id == 1L)
        phone.AuthApi_Whole().delete(1)

        // assert(phone.authApi_Multi.get() == 1)
        assert(phone.AuthApi_Multi().search(1).getOrThrow()?.id == 1L)
        phone.AuthApi_Multi().delete(1)
    }

    @Test
    fun jwtAuth() = start(
        configureServer = {
            Phone.route(routing { }, AuthApiImpl.Jwt)
        }
    ) { phone ->
        phone.AuthApi_Jwt().delete("f").getOrThrow()
    }

    @Test
    fun polymorphic() = start(
        configureServer = {
            Phone.route(routing { }, PolymorphicApiImpl)
        }
    ) { phone ->
        assert(phone.PolymorphicApi().foo().getOrThrow() == "foo")
        assert(phone.PolymorphicApi().foo(1L).getOrThrow() == 1L)
        assert(phone.PolymorphicApi().foo(1).getOrThrow() == 1)
    }

    @Test
    fun `super`() = start(
        configureServer = {
            Phone.route(routing { }, SuperInterfaceApiImpl)
        }
    ) { phone ->
        assert(phone.SuperInterfaceApi().foo().getOrThrow() == 1)
        assert(phone.SuperInterfaceApi().bar().getOrThrow() == 1)
    }

    @Test
    fun vararg() = start(
        configureServer = {
            Phone.route(routing { }, VarargApiImpl)
        }
    ) { phone ->
        assert(phone.VarargApi().sum(1, 2, 3).getOrThrow() == 6)
    }

    @Test
    fun customSerializer() = start(
        configureServer = {
            Phone.route(routing { }, CustomSerializerApiImpl)
        }
    ) { phone ->
        val a = Time(1, 2, 3)
        val b = Time(4, 5, 6)
        val ab = phone.CustomSerializerApi().sumTime(a, b).getOrThrow()
        assert(ab == Time(5, 7, 9))
    }

    @Test
    fun manual() = start(
        configureServer = {
            Phone.route(routing { }, ManualApiImpl)
        }
    ) { phone ->
        phone.ManualApi()
            .getIdLength("2")
            .getOrThrow()
            .let { (size, _) ->
                assert(size == 1)
            }

        phone.ManualApi()
            .getIdLength(null)
            .getOrThrow()
            .let { (size, _) ->
                assertNull(size)
            }

        phone.ManualApi()
            .directGet()
            .getOrThrow()
            .let {
                assert(it.first == 1L)
            }

        phone.ManualApi()
            .getUnit("S")

        phone.ManualApi {
                setBody(byteArrayOf(1))
            }
            .exchange("122")
            .getOrThrow()
            .let { (headInfo, response) ->
                assert(headInfo == listOf("122")) {
                    headInfo
                }
                val bytes = response.readBytes()
                assert(bytes.contentEquals(byteArrayOf(1))) {
                    bytes.joinToString()
                }
            }
    }

    @Test
    fun partialContent() = start(
        configureServer = {
            Phone.route(routing { }, PartialContentApiImpl)
            install(PartialContent) {
                maxRangeCount = 10
            }
            install(AutoHeadResponse)
        }
    ) { phone ->
        val path = ".gitignore"
        val file = File(path)
        val expectedBytes = file.readBytes()

        phone.PartialContentApi()
            .partialGet(path)
            .getOrThrow()
            .let {
                assert(it.tag.first == path)
                assert(it.tag.second == expectedBytes.size.toLong())
                assert(it.get().readBytes().contentEquals(expectedBytes))
                val partialBytes = it.get(0L..<2L, 2L..<3L).readBytes()

                assert(partialBytes.contentEquals(expectedBytes.take(3).toByteArray())){
                    partialBytes.toList()
                }
            }

        phone.PartialContentApi()
            .partialGetUnit("@")
            .getOrThrow()
            .let {
                assert(it.get().readBytes().contentEquals(expectedBytes))
            }
    }

    fun ws(withWss: Boolean) = start(
        configureServer = {
            Phone.route(routing { }, WebSocketApiImpl)
        },
        enables = withWss,
    ) { phone ->
        phone.WebSocketApi()
            .getSignals(1)
            .getOrThrow()
            .run {
                val text = (incoming.receive() as Frame.Text).readText()
                assert(text == "1") {
                    "isWss: $withWss $text"
                }
            }

        phone.WebSocketApi()
            .getChats("1")
            .getOrThrow()
            .run {
                val text = (incoming.receive() as Frame.Text).readText()
                assert(text == "1") {
                    "isWss: $withWss $text"
                }
            }
    }

    @Test
    fun ws(){
        ws(false)
        ws(true)
    }
}