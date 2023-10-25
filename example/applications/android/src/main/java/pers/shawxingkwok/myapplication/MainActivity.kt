package pers.shawxingkwok.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException

val client = HttpClient(CIO) {
    // engine {
        // connectTimeout = 100_000
        // socketTimeout = 100_000
        // hostname is different in each network.
        // proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("192.168.0.105", 80))
    // }
    install(WebSockets)
}
// val phone = Phone(client)

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val basicUrl = "http://192.168.0.105:80"

        lifecycleScope.launch {
            // val tv = requireViewById<TextView>(R.id.tv)
            client.get("$basicUrl/X")
                .bodyAsText()
                .let { Log.d("phone", "$it ${it.length}") }

            client.webSocket(
                path = "echo",
                host = "192.168.0.105", port = 80,
            ){
            // }
            // client.webSocket("$basicUrl/echo") {
                try {
                    val greetingText = (incoming.receive() as? Frame.Text)?.readText() ?: ""
                    assert("Please enter your name" == greetingText)
                    Log.d("phone", greetingText)
                    send(Frame.Text("JetBrains"))
                    val responseText = (incoming.receive() as Frame.Text).readText()
                    Log.d("phone", responseText)
                    assert("Hi, JetBrains!" == responseText)
                    send(Frame.Text("bye"))
                    val reason = (incoming.receive() as Frame.Close).readReason()
                    Log.d("phone", "$reason")
                } catch (e: ClosedReceiveChannelException) {
                    Log.d("phone", closeReason.await().toString())
                }
            }

            // runCatching {
            //     phone.chatApi.getChats()
            // }
            // .onFailure { tv.text = "failed getting chats" }
            // .onSuccess { tv.text = it.joinToString("\n") }
        }
    }
}