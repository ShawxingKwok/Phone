package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ksputil.simpleName

private val cache = mutableMapOf<KSClassDeclaration, List<KSFunctionDeclaration>>().alsoRegister()

internal fun KSClassDeclaration.getNeededFunctions(): List<KSFunctionDeclaration> =
    cache.getOrPut(this) {
        getAllFunctions().filter { it.isAbstract }.toList()
    }