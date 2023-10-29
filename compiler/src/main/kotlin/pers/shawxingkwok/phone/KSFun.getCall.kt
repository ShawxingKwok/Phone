package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ksputil.getAnnotationByType
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ktutil.fastLazy

internal sealed interface Call{
    val method: Phone.Method
    val polymorphicId: String?

    class Common(
        override val method: Phone.Method,
        override val polymorphicId: String?,
        val returnType: KSType,
        val typeRef: KSTypeReference
    ) : Call

    class Manual(
        override val method: Phone.Method,
        override val polymorphicId: String?,
        val tagType: KSType,
        val typeRef: KSTypeReference
    ) : Call

    class PartialContent(
        override val polymorphicId: String?,
        val tagType: KSType,
        val typeRef: KSTypeReference
    ) : Call{
        override val method = Phone.Method.Get
    }

    class WebSocket(
        override val method: Phone.Method,
        override val polymorphicId: String?,
        val isRaw: Boolean,
    ) : Call
}

private val cache = mutableMapOf<KSDeclaration, Any>().alsoRegister()

internal fun KSFunctionDeclaration.getCall(ksclass: KSClassDeclaration): Call =
    cache.getOrPut(this) {
        val defaultMethod =
            cache.getOrPut(ksclass) {
                ksclass.getAnnotationByType(Phone.Api::class)!!.method
                .takeUnless { it == Phone.Method.Default }
                ?: Args.defaultMethod
            } as Phone.Method

        val ksCallAnnot = annotations.first {
            it.annotationType.resolve().declaration.qualifiedName()!!.startsWith(Phone.Call::class.qualifiedName!!)
        }

        val firstArgTypeRef by fastLazy {
            ksCallAnnot.annotationType.resolve().arguments.first().type!!
        }

        val firstArgType by fastLazy { firstArgTypeRef.resolve() }

        when (ksCallAnnot.shortName.asString()) {
            Phone.Call.Common::class.simpleName -> {
                val call = getAnnotationByType(Phone.Call.Common::class)!!
                val method = call.method.takeUnless { it == Phone.Method.Default } ?: defaultMethod
                val polymorphicId = call.polymorphicId.takeIf { it.any() }
                Call.Common(method, polymorphicId, firstArgType, firstArgTypeRef)
            }

            Phone.Call.Manual::class.simpleName -> {
                val call = getAnnotationByType(Phone.Call.Manual::class)!!
                val method = call.method.takeUnless { it == Phone.Method.Default } ?: defaultMethod
                val polymorphicId = call.polymorphicId.takeIf { it.any() }
                Call.Manual(method, polymorphicId, firstArgType, firstArgTypeRef)
            }

            Phone.Call.PartialContent::class.simpleName -> {
                val call = getAnnotationByType(Phone.Call.PartialContent::class)!!
                val polymorphicId = call.polymorphicId.takeIf { it.any() }
                Call.PartialContent(polymorphicId, firstArgType, firstArgTypeRef)
            }

            Phone.Call.WebSocket::class.simpleName -> {
                val call = getAnnotationByType(Phone.Call.WebSocket::class)!!
                val polymorphicId = call.polymorphicId.takeIf { it.any() }
                Call.WebSocket(call.method, polymorphicId, call.isRaw)
            }

            else -> InFuture()
        }
    } as Call