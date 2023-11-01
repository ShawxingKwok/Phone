import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import org.w3c.dom.HTMLInputElement
import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.center.model.User
import pers.shawxingkwok.client.phone.Phone
import react.*
import react.dom.aria.AriaOrientation
import react.dom.aria.ariaOrientation
import react.dom.client.createRoot
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.span
import web.cssom.AutoLength
import web.cssom.AutoLengthProperty
import web.cssom.Margin
import web.cssom.atrule.orientation

val mainScope = MainScope()
val client = HttpClient(Js)
val phone = Phone(client)

fun main() {
    val container = web.dom.document.getElementById("root") ?: error("Couldn't find container!")
    val app = FC<Props> {
        var user by useState<User?>(null)
        if (user == null)
            loginScreen{ user = it }
        else
            welcomeScreen(user!!)
    }
    createRoot(container).render(app.create())
}

fun welcomeScreen(user: User){
    document.body!!.innerHTML = ""
    document.body!!.append {
        h1 {
            +"Hello, ${user.name}!"
        }
    }
}

fun ChildrenBuilder.loginScreen(setUser: (User) -> Unit){
    form {
        label {
            +"Username:"
            input {
                id = "idInput"
            }
        }

        label {
            +"Password:"
            input {
                this.type = web.html.InputType.password
                id = "passwordInput"
            }
        }

        button {
            +"Login"

            onClick = { event ->
                event.preventDefault()

                mainScope.launch {
                    val idInput = document.getElementById("idInput") as HTMLInputElement
                    val passwordInput = document.getElementById("passwordInput") as HTMLInputElement
                    val id = idInput.value.toLong()
                    val password = passwordInput.value

                    phone.DemoApi()
                        .login(id, password)
                        .onFailure {
                            window.alert(it.message ?: "Unexpected error!")
                        }
                        .onSuccess { result ->
                            when (result) {
                                is LoginResult.NotSigned -> window.alert(result.msg)

                                is LoginResult.PasswordWrong -> {
                                    window.alert(result.msg)
                                    // also check times of inputting a wrong password!
                                }

                                is LoginResult.Success -> setUser(result.user)
                            }
                        }
                }
            }
        }
    }
}