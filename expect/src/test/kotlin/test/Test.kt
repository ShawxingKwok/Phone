package test

import expect.TestApiImpl
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import pers.shawxingkwok.expect.server.Phone
import pers.shawxingkwok.expect.test.client.onReceivedSuccess
import kotlin.test.Test

class Test {
    @Test
    fun traditionalWebsocket() = testApplication {
        application {
            this@application.install(WebSockets)

            Phone.route(routing { }, TestApiImpl)
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

        pers.shawxingkwok.expect.test.client.Phone(client)
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

    @Test
    fun usePhone() = testApplication{
        application {
            this@application.install(WebSockets)
            Phone.route(routing {  }, TestApiImpl)
        }

        val client = createClient {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }
        val phone = pers.shawxingkwok.expect.test.client.Phone(client)

        phone.TestApi()
            .get(1)
            .getOrThrow()
            .run{
                assert((incoming.receive() as Frame.Text).readText() == "1")
                println("success")
            }

        phone.TestApi()
            .search("101")
            .getOrThrow()
            .let {
                assert(it?.id == "101")
            }
    }

    @Test
    fun file() = testApplication {
        application {
            this@application.install(WebSockets)
            Phone.route(routing {  }, TestApiImpl)
        }

        val phone = pers.shawxingkwok.expect.test.client.Phone(client)

        phone.TestApi{
                setBody(byteArrayOf(1))
            }
            .getFile("Titanic")
            .getOrThrow()
            .let {
                assert(it.first == "Titanic")
                assert(it.second.readBytes().contentEquals(byteArrayOf(1)))
            }
    }
}