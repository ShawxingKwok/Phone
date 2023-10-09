package pers.shawxingkwok.phone

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import pers.shawxingkwok.ksputil.simpleName

internal fun KSClassDeclaration.getNeededFunctions(): List<KSFunctionDeclaration> =
    getAllFunctions().filter {
        Modifier.SUSPEND in it.modifiers
        && it.isAbstract
        && it.simpleName() != "toString"
        && it.simpleName() != "equals"
        && it.simpleName() != "hashcode"
    }
    .toList()