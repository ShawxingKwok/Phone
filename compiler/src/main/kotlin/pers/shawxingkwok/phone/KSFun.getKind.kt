package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.Log
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ktutil.fastLazy

internal sealed interface Kind{
    class Common(val returnType: KSType) : Kind
    class Manual(val tagType: KSType) : Kind
    class PartialContent(val tagType: KSType) : Kind
    class WebSocket(val isRaw: Boolean) : Kind
}

private val cache = mutableMapOf<KSFunctionDeclaration, Kind>().alsoRegister()

internal val KSFunctionDeclaration.kind: Kind get() =
    cache.getOrPut(this) {
        val ksAnnot = annotations.first {
            it.annotationType.resolve().declaration.qualifiedName()!!.startsWith(Phone.Kind::class.qualifiedName!!)
        }
        val firstArgType by fastLazy {
            ksAnnot.annotationType.resolve().arguments.first().type!!.resolve()
        }
        when (ksAnnot.shortName.asString()) {
            Phone.Kind.Common::class.simpleName -> Kind.Common(firstArgType)
            Phone.Kind.Manual::class.simpleName -> Kind.Manual(firstArgType)
            Phone.Kind.PartialContent::class.simpleName -> Kind.PartialContent(firstArgType)
            Phone.Kind.WebSocket::class.simpleName -> {
                val isRaw = getAnnotationByType(Phone.Kind.WebSocket::class)!!.isRaw
                Kind.WebSocket(isRaw)
            }
            else -> InFuture()
        }
    }