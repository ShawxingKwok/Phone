package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import pers.shawxingkwok.ksputil.Log
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ktutil.fastLazy

internal sealed interface Kind{
    class Common(val returnType: KSType, val typeRef: KSTypeReference) : Kind
    class Manual(val tagType: KSType, val typeRef: KSTypeReference) : Kind
    class PartialContent(val tagType: KSType, val typeRef: KSTypeReference) : Kind
    class WebSocket(val isRaw: Boolean) : Kind
}

private val cache = mutableMapOf<KSFunctionDeclaration, Kind>().alsoRegister()

internal val KSFunctionDeclaration.kind: Kind get() =
    cache.getOrPut(this) {
        val ksAnnot = annotations.first {
            it.annotationType.resolve().declaration.qualifiedName()!!.startsWith(Phone.Kind::class.qualifiedName!!)
        }
        val firstArgTypeRef by fastLazy {
            ksAnnot.annotationType.resolve().arguments.first().type!!
        }
        val firstArgType by fastLazy { firstArgTypeRef.resolve() }

        when (ksAnnot.shortName.asString()) {
            Phone.Kind.Common::class.simpleName -> Kind.Common(firstArgType, firstArgTypeRef)
            Phone.Kind.Manual::class.simpleName -> Kind.Manual(firstArgType, firstArgTypeRef)
            Phone.Kind.PartialContent::class.simpleName -> Kind.PartialContent(firstArgType, firstArgTypeRef)
            Phone.Kind.WebSocket::class.simpleName -> {
                val isRaw = getAnnotationByType(Phone.Kind.WebSocket::class)!!.isRaw
                Kind.WebSocket(isRaw)
            }
            else -> InFuture()
        }
    }