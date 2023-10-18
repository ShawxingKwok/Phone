// package pers.shawxingkwok.test.server
//
// import io.ktor.server.application.*
// import io.ktor.server.websocket.*
// import pers.shawxingkwok.center.model.LoginResult
// import pers.shawxingkwok.center.model.User
//
// class MyWebSocketImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocket {
//     override suspend fun getChats() {
//         TODO("Not yet implemented")
//     }
// }
//
// class MyRawWebSocketImpl(override val session: WebSocketServerSession) : Phone.MyRawWebSocket{
//     override suspend fun getChats() {
//         TODO("Not yet implemented")
//     }
// }
//
// class MySubProtocolWebSocketImpl(override val session: DefaultWebSocketServerSession) : Phone.MySubProtocolWebSocket{
//     override suspend fun getChats() {
//         TODO("Not yet implemented")
//     }
// }
//
// class MyWebSocketWithArgsImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocketWithArgs{
//     override suspend fun getChats(id: Long, name: String) {
//         TODO("Not yet implemented")
//     }
//
// }
//
// class MyWebSocketWithAuthImpl(override val session: DefaultWebSocketServerSession) : Phone.MyWebSocketWithAuth{
//     override suspend fun getChats(id: Long, name: String) {
//         TODO("Not yet implemented")
//     }
// }