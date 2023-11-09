package pers.shawxingkwok.test.details

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.Test
import pers.shawxingkwok.test.server.Callback
import pers.shawxingkwok.test.server.Phone
import pers.shawxingkwok.test.util.testPhone
import java.security.MessageDigest
import java.util.*

class Auth {
    object JwtConfig {
        const val AUDIENCE = "jwt-audience"
        const val REALM = "ktor sample app"
        const val SECRET = "secret"
        const val ISSUER = "jwt issuer"
    }
    data class CustomPrincipal(val userName: String, val realm: String) : Principal

    private fun testAuthPhone(
        apiImpl: Any,
        configureServer: Application.() -> Unit = {},
        enablesWss: Boolean = false,
        act: suspend ApplicationTestBuilder.(pers.shawxingkwok.test.client.Phone) -> Unit
    ) =
        testPhone(
            apiImpl,
            configureServer = {
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
                        fun getMd5Digest(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(
                            str.toByteArray(
                                Charsets.UTF_8
                            )
                        )

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

                configureServer()
            },
            configureClient = {
                install(Auth) {
                    val myRealm = "Access to the '/' path"
                    basic {
                        credentials {
                            BasicAuthCredentials(username = "jetbrains", password = "foobar")
                        }
                        realm = myRealm
                    }

                    digest {
                        credentials {
                            DigestAuthCredentials(username = "jetbrains", password = "foobar")
                        }
                        realm = myRealm
                    }
                }
            },
            enablesWss = enablesWss,
            act = act
        )

    @Test
    fun partial() = testAuthPhone(
        apiImpl = object : Phone.AuthApi_Partial{
            override suspend fun delete(id: Int): Callback<Int> = { id }
        }
    ){
        assert(it.AuthApi_Partial().delete(1).getOrThrow() == 1)
    }

    @Test
    fun testWhole() = testAuthPhone(
        object : Phone.AuthApi_Whole{
            override suspend fun delete(id: Int): Callback<Int> = { id }
        }
    ){
        assert(it.AuthApi_Whole().delete(1).getOrThrow() == 1)
    }

    @Test
    fun multi() = testAuthPhone(
        object : Phone.AuthApi_Multi{
            override suspend fun delete(id: Int): Callback<Int> = { id }
        }
    ){
        assert(it.AuthApi_Multi().delete(1).getOrThrow() == 1)
    }

    @Test
    fun jwt() = testAuthPhone(
        object : Phone.AuthApi_Jwt{
            override suspend fun delete(id: Int): Callback<Int> = { id }
        }
    ){ phone ->
        assert(phone.AuthApi_Jwt().delete(1).isFailure)

        val token = JWT.create()
            .withAudience(JwtConfig.AUDIENCE)
            .withIssuer(JwtConfig.ISSUER)
            .withClaim("username", "shawxing")
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(Algorithm.HMAC256(JwtConfig.SECRET))

        phone.refreshJwtToken(token)

        assert(phone.AuthApi_Jwt().delete(1).getOrThrow() == 1)
    }

    object OAuth : Phone.AuthApi_Oauth{
        override fun Route.onStart() {
            get("/login/callback"){

            }
        }
        override suspend fun login(): Callback<Unit> = { }
    }
}