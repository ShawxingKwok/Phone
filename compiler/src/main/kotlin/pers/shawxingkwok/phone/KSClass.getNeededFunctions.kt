package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ksputil.simpleName

private val cache = mutableMapOf<KSClassDeclaration, List<KSFunctionDeclaration>>().alsoRegister()

internal fun KSClassDeclaration.getNeededFunctions(): List<KSFunctionDeclaration> =
    cache.getOrPut(this) {
        getAllFunctions()
            .filterNot {
                val name = it.simpleName()
                name == "toString"
                || name == "equals"
                || name == "hashCode"
            }
            .toList()
    }