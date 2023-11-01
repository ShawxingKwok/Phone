package server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import pers.shawxingkwok.server.phone.Phone
import java.time.Duration

fun main() {
    embeddedServer(Netty) { configure() }.start(wait = true)
}

private fun Application.configure() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // Don't do this in production if possible. Try to limit it.
    }

    Phone.routeAll(routing {  }, DemoApiImpl)
    routing {
        get("/A"){
            call.respondText("A", status = HttpStatusCode.OK)
        }
    }
}