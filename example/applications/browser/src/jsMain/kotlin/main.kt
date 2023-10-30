import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import pers.shawxingkwok.client.phone.Phone

val scope = MainScope()
val client = HttpClient(Js)
val phone = Phone(client, enablesWss = false)

fun main() {
    val container = document.createElement("div") as HTMLDivElement
    document.body!!.appendChild(container)
    container.style.fontSize = "100px"
    scope.launch {
        phone.AccountApi()
            .search(101)
            .onFailure {
                container.textContent = "${it.message}"
            }
            .onSuccess {
                container.textContent = when(it){
                    null -> "not found the user"
                    else -> "found ${it.name} in search"
                }
            }
    }
}