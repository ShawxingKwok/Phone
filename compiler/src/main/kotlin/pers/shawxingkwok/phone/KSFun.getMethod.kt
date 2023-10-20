package pers.shawxingkwok.phone

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.getAnnotationByType

internal enum class Method(val text: String){
    GET("get"),
    POST("post"),
    WEB_SOCKET("webSocket"),
    WEB_SOCKET_RAW("webSocketRaw")
}

internal fun KSFunctionDeclaration.getMethod(ksclass: KSClassDeclaration): Method =
    when{
        isAnnotationPresent(Phone.Get::class) -> Method.GET
        isAnnotationPresent(Phone.Post::class) -> Method.POST
        ksclass.isAnnotationPresent(Phone.Get::class) -> Method.GET
        ksclass.isAnnotationPresent(Phone.Post::class) -> Method.POST
        else -> {
            val webSocketAnnot = ksclass.getAnnotationByType(Phone.WebSocket::class)
            when(webSocketAnnot?.isRaw){
                true -> Method.WEB_SOCKET_RAW
                false -> Method.WEB_SOCKET
                else -> Args.defaultMethod
            }
        }
    }