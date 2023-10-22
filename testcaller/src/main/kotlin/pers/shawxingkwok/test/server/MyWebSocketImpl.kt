// package pers.shawxingkwok.test.server
//
// import io.ktor.server.application.*
// import io.ktor.server.websocket.*
// import io.ktor.websocket.*
// import pers.shawxingkwok.center.model.LoginResult
// import pers.shawxingkwok.center.model.User
//
// class MyWebSocketImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocket {
//     override suspend fun getChats() {
//         session.send("hello, world!")
//     }
// }
//
// class MyRawWebSocketImpl(override val session: WebSocketServerSession) : Phone.MyRawWebSocket{
//     override suspend fun getChats() {
//         session.send("hello, world!")
//     }
// }
//
// class MySubProtocolWebSocketImpl(override val session: DefaultWebSocketServerSession) : Phone.MySubProtocolWebSocket{
//     override suspend fun getChats() {
//         session.send("hello, world!")
//     }
// }
//
// class MyWebSocketWithArgsImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocketWithArgs{
//     override suspend fun getChats(id: Long, name: String) {
//         session.send("$id $name")
//     }
// }
//
// class MyWebSocketWithAuthImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocketWithAuth{
//     override suspend fun getChats() {
//         session.send("hello, world!")
//     }
// }