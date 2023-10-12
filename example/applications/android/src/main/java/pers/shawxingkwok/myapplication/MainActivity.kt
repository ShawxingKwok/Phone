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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import pers.shawxingkwok.client.phone.Phone

val client = HttpClient(Android)
val phone = Phone(client)

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            val loginResult = runCatching{
                phone.accountApi.login("15154@gmail.com", "123456")
            }
            val tv = requireViewById<TextView>(R.id.tv)
            tv.text = loginResult.getOrElse { "failed" }.toString()
        }
    }
}