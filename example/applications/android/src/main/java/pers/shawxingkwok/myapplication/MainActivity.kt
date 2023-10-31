package pers.shawxingkwok.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.launch

val client = HttpClient(Android) {
    defaultRequest {
        host = "10.0.2.2"
        port = 80
    }
    install(WebSockets)
}

val phone = Phone(client, baseUrl = "", enablesWss = false)

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tv = requireViewById<TextView>(R.id.tv)

        lifecycleScope.launch {
            phone.AccountApi()
                .search(101)
                .onFailure {
                    tv.text = "${it.message}"
                }
                .onSuccess {
                    tv.text = when (it) {
                        null -> "not found the user"
                        else -> "found ${it.name} in search"
                    }
                }
        }
    }
}