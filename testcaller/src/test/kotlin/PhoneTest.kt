import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.cio.*
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
        enablesWss: Boolean = false,
        configureClient: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit = {},
        requestOnClient: suspend ApplicationTestBuilder.(pers.shawxingkwok.test.client.Phone) -> Unit,
    ) =
        testApplication {
            application {
                configureServer()
                routing {
                    options {

                    }
                    head {

                    }
                }
            }

            val client = createClient {
                configureClient()

                install(io.ktor.client.plugins.websocket.WebSockets)


            }
            val phone = pers.shawxingkwok.test.client.Phone(client, enablesWss = enablesWss)

            // phone.refreshJwtToken(token)

            requestOnClient(phone)
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

        val file = File(".gitignore")
        phone.ManualApi {
                val channel = file.readChannel()
                setBody(channel)
            }
            .exchange("122")
            .getOrThrow()
            .let { (headInfo, response) ->
                assert(headInfo == listOf("122")) {
                    headInfo
                }
                val bytes = response.readBytes()
                assert(bytes.contentEquals(file.readBytes())) {
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

                assert(partialBytes.contentEquals(expectedBytes.take(3).toByteArray())) {
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
        enablesWss = withWss,
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
    fun ws() {
        ws(false)
        ws(true)
    }
}