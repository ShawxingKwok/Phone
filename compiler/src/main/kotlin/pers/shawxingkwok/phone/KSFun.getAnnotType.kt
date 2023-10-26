package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ktutil.getOrPutNullable
import kotlin.reflect.KClass

private val cache = mutableMapOf<Any, KSType?>().alsoRegister()

private fun <T: Annotation> KSFunctionDeclaration.getAnnotationType(kclass: KClass<T>) =
    cache.getOrPutNullable(this to kclass){ _ ->
        annotations.firstOrNull {
            it.shortName.getShortName() == kclass.simpleName
            && it.annotationType.resolve().declaration.qualifiedName?.asString() == kclass.qualifiedName
        }
        .let { it ?: return@getOrPutNullable null }
        .annotationType
        .resolve()
    }

internal val KSFunctionDeclaration.commonReturnType: KSType? get() {
    val commonType = getAnnotationType(Phone.Common::class) ?: return null
    return commonType.arguments.first().type!!.resolve()
}

internal val KSFunctionDeclaration.fileTrgType: KSType? get() {
    val commonType = getAnnotationType(Phone.File::class) ?: return null
    return commonType.arguments.first().type!!.resolve()
}