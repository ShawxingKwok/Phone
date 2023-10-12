package pers.shawxingkwok.center.client

import io.ktor.client.*

val phone = Phone(HttpClient())

suspend fun main() {
    phone.timeApi.getTime()
    phone.accountApi.delete(1)
    phone.chatApi.getChats()
}