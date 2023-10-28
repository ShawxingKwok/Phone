package test

import expect.TestApiImpl
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import pers.shawxingkwok.expect.server.Phone
import pers.shawxingkwok.expect.test.client.onReceivedSuccess
import java.io.File
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
        client.submitForm()
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
                assert(it.first == "Titanic".length)
                assert(it.second.readBytes().contentEquals(byteArrayOf(1)))
            }
    }

    @Test
    fun partialFile() = testApplication {
        application {
            this@application.install(WebSockets)
            install(AutoHeadResponse)
            install(PartialContent) {
                // Maximum number of ranges that will be accepted from a HTTP request.
                // If the HTTP request specifies more ranges, they will all be merged into a single range.
                maxRangeCount = 10
            }

            Phone.route(routing {  }, TestApiImpl)
        }

        val phone = pers.shawxingkwok.expect.test.client.Phone(client)

        val name = ".gitignore"

        phone.TestApi()
            .getPartialFile(name)
            .getOrThrow()
            .run {
                val file = File(name)
                val expectedBytes = file.readBytes()
                assert(tag == expectedBytes.size){ tag.toString() }

                val max = expectedBytes.size
                var start = 0L
                var bytes = byteArrayOf()

                while (start < max){
                    bytes += get(start..< start + 2).readBytes()
                    println(bytes.toList())
                    start += 2
                }

                assert(bytes.contentEquals(file.readBytes()))
            }
    }

    @Test
    fun repeatRespond() = testApplication {
        application {
        }
        client.post("X").status.let(::println)
    }

    @Test
    fun putWithForm() = testApplication {
        application {
            routing {
                put("/X"){
                    call.receiveParameters().get("A").let(::println)
                }
            }
        }

        client.put("/X"){
            setBody(FormDataContent(parameters {
                append("A", "a")
            }))
        }
    }
}