package pers.shawxingkwok.test.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: Long, val name: String, val age: Int)