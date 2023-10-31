package pers.shawxingkwok.center.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Int,
    val text: String,
    val from: User
)