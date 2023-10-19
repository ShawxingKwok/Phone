import io.ktor.client.plugins.websocket.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.json.Json
import pers.shawxingkwok.center.Cipher
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.test.server.*
import kotlin.test.Test

class ApplicationTest {
    fun ApplicationTestBuilder.configureServer(){
        application {
            Phone.configure(
                route = routing { },
                ::AccountApiImpl,
                ::CryptoApi_PartialImpl,
                ::CryptoApi_WholeImpl,
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
}