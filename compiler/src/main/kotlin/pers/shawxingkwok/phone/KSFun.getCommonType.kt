package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ktutil.getOrPutNullable

private val cache = mutableMapOf<KSFunctionDeclaration, KSType?>().alsoRegister()

internal val KSFunctionDeclaration.commonType: KSType? get() =
    cache.getOrPutNullable(this){
        annotations.firstOrNull {
            it.shortName.getShortName() == Phone.Common::class.simpleName
            && it.annotationType.resolve().declaration.qualifiedName?.asString() == Phone.Common::class.qualifiedName
        }
        .let { it ?: return@getOrPutNullable null }
        .annotationType
        .resolve()
        .arguments
        .first()
        .type!!
        .resolve()
    }