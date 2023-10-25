package test

import expect.*
import expect.TestApiImpl
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.cio.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlin.test.Test

class Test {
    @Test
    fun websocket() = testApplication {
        application {
            this@application.install(WebSockets)
            WebSocketServerPhone.route(routing { }, ::TestApiImpl)
        }

        val client = createClient {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }
        client.webSocket(
            urlString = "/TestApi/get",
            request = {
                parameter("i", 1)
            }
        ){
            println("33")
        }

        WebSocketClientPhone(client)
            .TestApi()
            .get(1)
            .onFailure {
                println("fail $it" + ".".repeat(10))
            }
            .onReceivedSuccess {
                val textFrame = incoming.receive() as Frame.Text
                val text = textFrame.readText()
                assert(text == "1") { text }
                println("success")
            }
    }
}