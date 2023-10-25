package expect

import io.ktor.client.plugins.websocket.*

inline fun <T: ClientWebSocketSession> Result<T>.onReceivedSuccess(act: T.() -> Unit){
    onSuccess{ it.act() }
}