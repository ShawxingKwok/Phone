package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.alsoRegister

private val cache = mutableMapOf<KSClassDeclaration, List<KSFunctionDeclaration>>().alsoRegister()

internal fun KSClassDeclaration.getNeededFunctions(): List<KSFunctionDeclaration> =
    cache.getOrPut(this) {
        getAllFunctions().filter { it.isAbstract }.toList()
    }