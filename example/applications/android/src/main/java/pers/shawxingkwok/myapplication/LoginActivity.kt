package pers.shawxingkwok.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.android.*
import kotlinx.coroutines.launch
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.client.phone.Phone
import pers.shawxingkwok.myapplication.databinding.ActivityLoginBinding

val client = HttpClient(Android)
val phone = Phone(client, "http://10.0.2.2:80")

lateinit var user: User
    private set

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString().toLongOrNull()
                ?: return@setOnClickListener showShortToast("Invalid id!")

            val password = binding.etPassword.text.toString()
            if (password.none())
                return@setOnClickListener showShortToast("No password!")

            lifecycleScope.launch {
                phone.DemoApi()
                    .login(id, password)
                    .onFailure {
                        showShortToast(it.message ?: "Unexpected error!")
                    }
                    .onSuccess { result ->
                        showShortToast(result.msg)

                        when(result){
                            is LoginResult.NotSigned -> {}

                            is LoginResult.PasswordWrong -> {
                                // check times of inputting a wrong password!
                            }

                            is LoginResult.Success ->{
                                user = result.user
                                // navigate to another page
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
            }
        }
    }

    private fun showShortToast(msg: String){
        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
    }
}