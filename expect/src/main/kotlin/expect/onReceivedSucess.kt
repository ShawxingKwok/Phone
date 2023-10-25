package expect

import io.ktor.client.plugins.websocket.*

public inline fun <T: ClientWebSocketSession> Result<T>.onReceivedSuccess(act: T.() -> Unit){
    onSuccess{ it.act() }
}