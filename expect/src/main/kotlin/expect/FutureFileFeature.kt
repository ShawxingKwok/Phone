package expect

import io.ktor.client.statement.*
import java.io.File

@Repeatable
annotation class PhoneFile(val parameters: Array<Param<*>>){
    annotation class Param<T>(val name: String)
}

@PhoneFile([PhoneFile.Param<String>("name")])
interface DownloadFileContract<P, R>{
    fun get(p: P): R
}

data class DownloadInfo(val name: String)

interface DownloadFileApi : DownloadFileContract<Unit, Pair<String, File>>

class DownloadFileApiImpl : DownloadFileApi {
    override fun get(p: Unit): Pair<String, File> {
        TODO("Not yet implemented")
    }
}

class DownloadFileCommonServiceImpl : DownloadFileContract<Unit, Pair<String, ByteArray>> {
    override fun get(p: Unit): Pair<String, ByteArray> {
        TODO("Not yet implemented")
    }
}

class DownloadFileChannelServiceImpl : DownloadFileContract<(String, HttpResponse) -> Unit, Unit> {
    override fun get(p: (String, HttpResponse) -> Unit) {
        TODO("Not yet implemented")
    }
}