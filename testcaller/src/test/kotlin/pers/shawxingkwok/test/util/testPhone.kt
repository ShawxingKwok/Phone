package pers.shawxingkwok.test.util

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import pers.shawxingkwok.test.client.Phone
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters

fun testPhone(
    api: Any,
    configureServer: Application.() -> Unit = {},
    configureClient: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit = {},
    enablesWss: Boolean = false,
    act: suspend ApplicationTestBuilder.(Phone) -> Unit
) =
    testApplication {
        val client = createClient(configureClient)
        val phone = Phone(client, enablesWss = enablesWss)

        application {
            configureServer()

            pers.shawxingkwok.test.server.Phone::class.declaredFunctions
            .filter { it.name == "route" }
            .first {
                val paramType = it.valueParameters[1].type
                val argType = api::class.starProjectedType

                paramType == argType || paramType.isSupertypeOf(argType)
            }
            .call(pers.shawxingkwok.test.server.Phone, routing { }, api)
        }

        act(phone)
    }