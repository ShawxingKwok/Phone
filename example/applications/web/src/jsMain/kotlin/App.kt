import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.coroutines.MainScope
// import pers.shawxingkwok.client.phone.Phone

// val phone = Phone(client)
// val scope = MainScope()

fun main() {
    val client = HttpClient(Js)
    console.log("Hello, Kotlin/JS!")
}