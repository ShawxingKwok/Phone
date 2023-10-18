package pers.shawxingkwok.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.launch
import pers.shawxingkwok.client.phone.Phone
import java.net.InetSocketAddress
import java.net.Proxy

val client = HttpClient(Android) {
    engine {
        connectTimeout = 100_000
        socketTimeout = 100_000
        // hostname is different in each network.
        proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("192.168.0.105", 8080))
    }
}
val phone = Phone(client)

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            val tv = requireViewById<TextView>(R.id.tv)

            runCatching {
                phone.chatApi.getChats()
            }
            .onFailure { tv.text = "failed getting chats" }
            .onSuccess { tv.text = it.joinToString("\n") }
        }
    }
}