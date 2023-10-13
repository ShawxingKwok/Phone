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
import kotlinx.coroutines.launch
import pers.shawxingkwok.client.phone.Phone

val client = HttpClient(Android)
val phone = Phone(client)

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            val tv = requireViewById<TextView>(R.id.tv)

            runCatching{
                phone.chatApi.getChats()
            }
            .onFailure { tv.text = "failed getting chats" }
            .onSuccess { tv.text = it.joinToString("\n") }
        }
    }
}