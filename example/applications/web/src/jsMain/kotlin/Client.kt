import csstype.HtmlAttributes
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.client.phone.Phone

val scope = MainScope()
val client = HttpClient(Js)
val phone = Phone(client)

fun main() {
    val container = document.createElement("div") as HTMLDivElement
    document.body!!.appendChild(container)
    container.style.fontSize = "100px"

    scope.launch {
        runCatching { phone.accountApi.search(101) }
        .onFailure { container.textContent = "failed connection" }
        .onSuccess {
            container.textContent = when(it){
                null -> "not found the user"
                else -> "found ${it.name} in search"
            }
        }
        client.submitForm(
            url = "/X",
            parameters {
                append("x", "1")
            }
        )
    }
}