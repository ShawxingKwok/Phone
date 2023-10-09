package pers.shawxingkwok.phonesample.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

sealed interface LoginResult {
    // value class could boost some performances
    @Serializable
    @JvmInline
    value class Success(val user: User) : LoginResult

    @Serializable
    object NotSigned : LoginResult

    @Serializable
    object PasswordWrong : LoginResult
}