package pers.shawxingkwok.center.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface LoginResult {
    // value class could boost some performances
    @Serializable
    data class Success(val user: User) : LoginResult

    @Serializable
    data object NotSigned : LoginResult

    @Serializable
    data object PasswordWrong : LoginResult
}