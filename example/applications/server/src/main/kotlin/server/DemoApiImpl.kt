package server

import pers.shawxingkwok.center.model.LoginResult
import pers.shawxingkwok.server.phone.Phone
import pers.shawxingkwok.server.phone.PipelineContextProvider
import pers.shawxingkwok.server.phone.WebSocketConnector

object DemoApiImpl : Phone.DemoApi {
    override suspend fun login(id: String, password: String): PipelineContextProvider<LoginResult> {
        TODO("Not yet implemented")
    }

    override suspend fun getChats(groupId: String): WebSocketConnector {
        TODO("Not yet implemented")
    }
}