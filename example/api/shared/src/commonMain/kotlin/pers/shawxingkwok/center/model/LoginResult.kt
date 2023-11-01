package pers.shawxingkwok.center.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface LoginResult {
    val msg: String

    @Serializable
    data class Success(val user: User) : LoginResult{
        override val msg: String = "Login successfully"
    }

    @Serializable
    data object NotSigned : LoginResult{
        override val msg: String = "Not registered"
    }

    @Serializable
    data object PasswordWrong : LoginResult{
        override val msg: String = "Wrong password"
    }
}