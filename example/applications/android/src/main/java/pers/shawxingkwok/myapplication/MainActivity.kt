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

        }
    }
}