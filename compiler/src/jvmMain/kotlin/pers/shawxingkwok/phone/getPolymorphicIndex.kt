package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import pers.shawxingkwok.ksputil.simpleName

internal fun KSFunctionDeclaration.getMayPolymorphicText(): String{
    val i = (parentDeclaration as KSClassDeclaration)
        .getNeededFunctions()
        .filter { it.simpleName() == this.simpleName() }
        .takeIf { it.size > 1 }
        ?.indexOf(this)

    return buildString {
        append(simpleName())
        if (i != null) append(i)
    }
}