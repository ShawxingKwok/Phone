import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import java.time.Duration

internal fun Application.installPlugins(){
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
}