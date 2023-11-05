package pers.shawxingkwok.test.details

import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.util.*
import io.ktor.websocket.*
import io.ktor.websocket.*
import org.junit.Test
import pers.shawxingkwok.center.model.Time
import pers.shawxingkwok.test.client.onReceivedSuccess
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.server.WebSocketConnector
import pers.shawxingkwok.test.util.testPhone
import java.io.File
import java.time.Duration

private val file = File(".gitignore")

class Compound {
    object Impl : Phone.CompoundApi {
        override suspend fun foo(i: Int): Callback<Time> = {
            Time(i, i, i)
        }

        override suspend fun foo(i: Long): Callback<Time> = {
            call.respondFile(file)
            Time(i.toInt(), i.toInt(), i.toInt())
        }

        override suspend fun foo(i: Byte): Callback<Time> = {
            call.respondFile(file)
            Time(i.toInt(), i.toInt(), i.toInt())
        }

        override suspend fun foo(i: Short): WebSocketConnector = {
            send("$i")
        }
    }

    val myRealm = "Access to the '/' path"

    val expectTime = Time(1,1,1)

    @Test
    fun start() = testPhone(
        api = Impl,
        configureServer = {
            install(WebSockets)
            install(Authentication) {
                basic {
                    realm = myRealm
                    validate { credentials ->
                        if (credentials.name == "jetbrains" && credentials.password == "foobar") {
                            UserIdPrincipal(credentials.name)
                        } else {
                            null
                        }
                    }
                }
            }
            install(PartialContent) {
                maxRangeCount = 10
            }
            install(AutoHeadResponse)
        },
        configureClient = {
            install(io.ktor.client.plugins.websocket.WebSockets)

            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "jetbrains", password = "foobar")
                    }
                    realm = myRealm
                }
            }
        }
    ) { phone ->

        assert(phone.CompoundApi().foo(1).getOrThrow() == expectTime)

        run {
            val (tag, resp) = phone.CompoundApi().foo(1L).getOrThrow()
            assert(tag == expectTime)
            assert(resp.readBytes().contentEquals(file.readBytes()))
        }
        run {
            val handler = phone.CompoundApi().foo(1.toByte()).getOrThrow()
            assert(handler.tag == expectTime)
            assert(handler.get().readBytes().contentEquals(file.readBytes()))
        }

        // The configured basic auth info in client is invalid for websockets.
        // Jwt is used in common WebSocket cases.
        phone.CompoundApi{
                header(HttpHeaders.Authorization, "Basic " + "jetbrains:foobar".encodeBase64())
            }
            .foo(1.toShort())
            .getOrThrow()
            .let {
                assert((it.incoming.receive() as Frame.Text).readText() == "1")
            }
    }
}