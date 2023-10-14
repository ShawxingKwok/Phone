import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pers.shawxingkwok.client.phone.Phone
import react.create
import react.dom.client.createRoot

val client = HttpClient(Js)
val phone = Phone(client)
val scope = MainScope()

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    scope.launch {
        runCatching {
            phone.chatApi.getChats()
        }
        .onFailure { console.log("line 18 failed") }
        .onSuccess {
            console.log(it)
            container.textContent = it.joinToString("\n")
        }
    }
}