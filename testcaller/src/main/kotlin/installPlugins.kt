import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import java.security.MessageDigest
import java.time.Duration

data class CustomPrincipal(val userName: String, val realm: String) : Principal

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

        digest("auth-digest") {
            fun getMd5Digest(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray(Charsets.UTF_8))

            val myRealm = "Access to the '/' path"

            val userTable: Map<String, ByteArray> = mapOf(
                "jetbrains" to getMd5Digest("jetbrains:$myRealm:foobar"),
                "admin" to getMd5Digest("admin:$myRealm:password")
            )
            realm = myRealm
            digestProvider { userName, _ ->
                userTable[userName]
            }
            validate { credentials ->
                if (credentials.userName == "jetbrains") {
                    CustomPrincipal(credentials.userName, credentials.realm)
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
                println("62: $defaultScheme $realm ${this.call.request.header(HttpHeaders.Authorization)}")
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