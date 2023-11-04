import io.ktor.server.routing.*
import io.ktor.server.testing.*
import pers.shawxingkwok.test.client.Phone
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

fun <T: Any> testPhone(
    apiImpl: T,
    enablesWss: Boolean = false,
    act: suspend ApplicationTestBuilder.(Phone) -> Unit
) =
    testApplication {
        val phone = Phone(client, enablesWss = enablesWss)

        application {
            pers.shawxingkwok.test.server.Phone::class.declaredFunctions
            .first {
                it.name == "route"
                && it.valueParameters[1].type.isSupertypeOf(apiImpl::class.starProjectedType)
            }
            .call(pers.shawxingkwok.test.server.Phone, routing { }, apiImpl)
        }

        act(phone)
    }