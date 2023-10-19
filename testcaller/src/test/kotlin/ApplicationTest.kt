import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.json.Json
import pers.shawxingkwok.center.Cipher
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.test.server.*
import java.util.*
import kotlin.test.Test

class ApplicationTest {
    fun ApplicationTestBuilder.configureServer(act: Application.() -> Unit = {}){
        application {
            act()

            Phone.route(
                route = routing { },
                ::AccountApiImpl,
                AuthApiImpl::Partial,
                AuthApiImpl::Whole,
                CryptoApiImpl::Partial,
                CryptoApiImpl::Whole,
                // ::NestXApiImpl,
                // ::PolymorphicApiImpl,
                // ::SuperInterfaceApiImpl,
                // ::VarargApiImpl,
                // ::MyWebSocketImpl,
                // ::MyRawWebSocketImpl,
                // ::MySubProtocolWebSocketImpl,
                // ::MyWebSocketWithArgsImpl,
                // ::MyWebSocketWithAuthImpl,
            )
        }
    }

    @Test
    fun commonAccount() = testApplication{
        configureServer()

        val phone = pers.shawxingkwok.test.client.Phone(client)
        assert(phone.accountApi.login("101", "") == LoginResult.NotSigned)
        assert(phone.accountApi.search(101)?.id == 101L)
        phone.accountApi.delete(0)
    }

    @Test
    fun crypto() = testApplication{
        configureServer()

        val phone = pers.shawxingkwok.test.client.Phone(client)
        assert(phone.cryptoApi_Partial.getChats(1, "a", "b") == listOf("1", "a", "b"))
        assert(phone.cryptoApi_Whole.getChats(1, "a") == listOf("1", "a"))
    }

    @Test
    fun cipher(){
        var text = "hello"
        var bytes = Cipher.encrypt(text.encodeToByteArray())
        text = Json.encodeToString(ByteArraySerializer(), bytes)
        bytes = Json.decodeFromString(ByteArraySerializer(), text)
        bytes = Cipher.decrypt(bytes)
        text = bytes.decodeToString()
        assert(text == "hello")
    }

    @Test
    fun auth() = testApplication{
        configureServer{
            this@configureServer.install(Authentication) {
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
            }
        }

        val client = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "jetbrains", password = "foobar")
                    }
                    realm = "Access to the '/' path"
                }
            }
        }

        val phone = pers.shawxingkwok.test.client.Phone(client)

        assert(phone.authApi_Partial.search(1)?.id == 1L)
        phone.authApi_Partial.delete(1)

        assert(phone.authApi_Whole.search(1)?.id == 1L)
        phone.authApi_Whole.delete(1)
    }
}